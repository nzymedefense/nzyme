use std::fmt;
use std::fmt::{Display};
use std::sync::{Arc, Mutex};
use std::time::Duration;
use crossbeam_channel::Receiver;
use log::{debug, error, info, trace, warn};
use crate::messagebus::bus::Bus;
use crate::metrics::Metrics;
use crate::peripherals::cobs::{cobs_decode, cobs_encode};
use crate::usb::usb::find_first_nzyme_usb_device_with_pid_and_serial;
use crate::wireless::dot11::frames::Dot11CaptureSource::Acquisition;
use crate::wireless::dot11::frames::Dot11RawFrame;
use crate::wireless::dot11::sona::commands::SonaCommand;
use crate::wireless::dot11::sona::sona::SONA_1_PID;
use crate::wireless::dot11::sona::sona_frame_header::SonaFrameHeader;

const MAX_ACCUMULATED_BYTES_WITHOUT_DELIMITER: usize = 8192;

const MSG_TYPE_DOT11: u8 = 1;
const MSG_TYPE_METRICS: u8 = 3;

pub struct Capture {
    pub metrics: Arc<Mutex<Metrics>>,
    pub bus: Arc<Bus>,
    pub command_receiver: Receiver<SonaCommand>,
}

#[derive(Debug, Clone)]
pub struct SonaMetrics {
    pub uptime_ms: u32,
    pub last_reset_reason: u32,
    pub temperature_mc: i32,
    pub frame_queue_used: u32,
    pub frame_queue_drops: u32,
}

impl fmt::Display for SonaMetrics {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        let uptime_sec = self.uptime_ms as f64 / 1000.0;
        let temp_celsius = self.temperature_mc as f64 / 1000.0;
        let reset_reason_str = reset_reason_to_string(self.last_reset_reason);

        write!(
            f,
            "SonaMetrics {{ uptime: {:.2}s, temp: {:.1}°C, queue: {}, drops: {}, reset: {} }}",
            uptime_sec,
            temp_celsius,
            self.frame_queue_used,
            self.frame_queue_drops,
            reset_reason_str
        )
    }
}

impl Capture {
    pub fn run(&self, interface_name: &str, serial: &str) {
        info!("Initializing Sona with serial [{}].", serial);

        let sona = match find_first_nzyme_usb_device_with_pid_and_serial(SONA_1_PID, serial) {
            Ok(Some(sona)) => sona,
            Ok(None) => {
                error!("No Sona with serial [{}] found.", serial);
                return;
            }
            Err(e) => {
                error!("Could not search for Sona with serial [{}]: {}", serial, e);
                return;
            }
        };

        let acm_port = match sona.acm_port {
            Some(acm_port) => acm_port,
            None => {
                error!("Found Sona with serial [{}] at [{}:{}] but it is not exposing \
                    an ACM port.", serial, sona.bus, sona.address);
                return;
            }
        };

        info!("Found Sona with serial [{}] at [{}:{}]: {}",
            serial, sona.bus, sona.address, acm_port);

        // Open CDC-ACM. (Baud is ignored by host, but we need to set it to something)
        let mut port_handle = match serialport::new(acm_port.clone(), 115_200)
            .timeout(Duration::from_millis(50))
            .open() {

            Ok(mut port) => {
                if let Err(e) = port.write_data_terminal_ready(true) {
                    error!("Failed to set DTR on ACM port [{}]: {}", acm_port, e);
                    return;
                }

                info!("DTR set on Sona [{}]", serial);
                port
            },
            Err(e) => {
                error!("Could not open ACM port [{}] of Sona with serial [{}] at [{}:{}]: {}",
            acm_port, serial, sona.bus, sona.address, e);
                return;
            }
        };

        info!("Connected to Sona [{}] ACM port. Starting data capture.", serial);

        // Buffers.
        let mut chunk = [0u8; 512];
        let mut acc: Vec<u8> = Vec::with_capacity(MAX_ACCUMULATED_BYTES_WITHOUT_DELIMITER);

        loop {
            // Write to port if we have any pending commands.
            while let Ok(cmd) = self.command_receiver.try_recv() {
                match cmd {
                    SonaCommand::SetFrequency(freq_mhz) => {
                        if let Err(e) = send_set_frequency(&mut *port_handle, freq_mhz as u16) { // TODO don't cast
                            warn!("Failed to send SetFrequency({}) to Sona [{}]: {}", freq_mhz, serial, e);
                        } else {
                            debug!("Sent SetFrequency({}) to Sona [{}].", freq_mhz, serial);
                        }
                    }
                    _ => {
                        // handle other commands later
                    }
                }
            }

            // Read from port.
            match port_handle.read(&mut chunk) {
                Ok(n) if n > 0 => {
                    if acc.len() + n > MAX_ACCUMULATED_BYTES_WITHOUT_DELIMITER {
                        warn!("Accumulator exceeded maximum bytes without delimiter: <{}> bytes, \
                            clearing buffer.", MAX_ACCUMULATED_BYTES_WITHOUT_DELIMITER);
                        acc.clear();
                        continue;
                    }

                    // Fill buffer.
                    acc.extend_from_slice(&chunk[..n]);

                    // Process all complete frames.
                    let mut processed_up_to = 0;

                    // TODO clean this up and extract into fns

                    while let Some(delimiter_pos) = acc[processed_up_to..]
                        .iter().position(|&b| b == 0) {

                        let frame_end = processed_up_to + delimiter_pos;

                        if frame_end > processed_up_to {
                            // Non-empty frame
                            let encoded = &acc[processed_up_to..frame_end];

                            match cobs_decode(encoded) {
                                Ok(decoded) => {
                                    if decoded.len() < 4 {
                                        error!("Dropping frame that is too short for message \
                                            header: <{}> bytes.", decoded.len());
                                        continue;
                                    }

                                    // Parse message header.
                                    let msg_type = decoded[0];
                                    let version = decoded[1];
                                    let payload_len = u16::from_le_bytes(
                                        [decoded[2], decoded[3]]
                                    ) as usize;

                                    if version != 1 {
                                        warn!("Unknown message version: {}", version);
                                        continue;
                                    }

                                    if decoded.len() < 4 + payload_len {
                                        error!("Frame too short for declared payload length");
                                        continue;
                                    }

                                    let payload = &decoded[4..4 + payload_len];

                                    match msg_type {
                                        MSG_TYPE_DOT11 => {
                                            // 802.11 frame message.
                                            if payload.len() < SonaFrameHeader::BYTES {
                                                error!("Dropping 802.11 frame that is too short \
                                                    to fit Sona header: <{}> bytes.", payload.len());
                                            } else {
                                                // Parse frame header.
                                                let header_bytes = &payload[..SonaFrameHeader::BYTES];
                                                if let Some(hdr) = SonaFrameHeader::parse(header_bytes) {
                                                    /* We should always have an RSSI, but, if not, skip
                                                     * the frame.
                                                     */
                                                    if let Some(rssi) = hdr.rssi_dbm {
                                                        let frame = &payload[SonaFrameHeader::BYTES..];

                                                        let radiotap = Self::create_radiotap_header(
                                                            hdr.rate_flags,
                                                            hdr.rate_code,
                                                            hdr.freq_mhz,
                                                            rssi
                                                        );

                                                        // Construct full frame.
                                                        let mut full_frame = Vec::with_capacity(
                                                            frame.len() + radiotap.len()
                                                        );
                                                        full_frame.extend_from_slice(&radiotap);
                                                        full_frame.extend_from_slice(frame);
                                                        let full_frame_length = full_frame.len();

                                                        let data = Dot11RawFrame {
                                                            capture_source: Acquisition,
                                                            interface_name: format!("sona-{}", serial),
                                                            data: full_frame
                                                        };

                                                        match self.metrics.lock() {
                                                            Ok(mut metrics) => {
                                                                metrics.increment_processed_bytes_total(full_frame_length as u32);
                                                                metrics.update_capture(
                                                                    interface_name, true, 0, 0
                                                                );
                                                            },
                                                            Err(e) => error!("Could not acquire metrics mutex: {}", e)
                                                        }

                                                        // Write to 802.11 pipeline.
                                                        match self.bus.dot11_broker.sender.lock() {
                                                            Ok(mut sender) => {
                                                                sender.send_packet(
                                                                    Arc::new(data),
                                                                    full_frame_length as u32
                                                                )
                                                            },
                                                            Err(e) => {
                                                                error!("Could not acquire 802.11 handler  broker mutex: {}", e)
                                                            }
                                                        }
                                                    } else {
                                                        trace!("Skipping Sona frame with no RSSI.");
                                                    }
                                                } else {
                                                    warn!("Could not parse frame header: [{:?}]", header_bytes);
                                                }
                                            }
                                        }
                                        MSG_TYPE_METRICS => {
                                            // Metrics message.
                                            if let Some(metrics) = parse_metrics_payload(payload) {
                                                info!("Sona [{}] metrics frame: [{}]", serial, metrics);
                                            } else {
                                                warn!("Failed to parse metrics payload.");
                                            }
                                        }
                                        _ => {
                                            debug!("Unknown message type: {}", msg_type);
                                        }
                                    }
                                }
                                Err(e) => {
                                    debug!("COBS decode error: {}", e);
                                }
                            }
                        }

                        // Move past this frame and its delimiter
                        processed_up_to = frame_end + 1;
                    }

                    // Remove all processed data in one operation
                    if processed_up_to > 0 {
                        acc.drain(0..processed_up_to);
                    }
                }
                Ok(_) => {
                    // No bytes read. Nothing to do.
                }
                Err(ref e) if e.kind() == std::io::ErrorKind::TimedOut => {
                    // No bytes read and timeout exceeded. Nothing to do.
                }
                Err(e) => {
                    error!("Sona [{}] ACM port [{}] read error: {}", serial, acm_port, e);
                    return;
                }
            }
        }
    }

    fn create_radiotap_header(flags: u8, rate: u8, frequency: u16, rssi: f32) -> Vec<u8> {
        let mut radiotap = Vec::new();

        // Fixed part of header.
        radiotap.push(0); // Version.
        radiotap.push(0); // Pad.
        radiotap.extend_from_slice(&0u16.to_le_bytes()); // Length placeholder. (set later)

        // Present word 0.
        let present_word0: u32 =
            (1u32 << 1)  | // Flags.
            (1u32 << 2)  | // Rate.
            (1u32 << 3)  | // Channel.
            (1u32 << 5)  | // dBm Antenna Signal.
            (1u32 << 11) | // Antenna.
            (1u32 << 14);  // RX Flags.

        radiotap.extend_from_slice(&present_word0.to_le_bytes());

        // Flags.
        radiotap.push(flags);

        // Rate.
        radiotap.push(rate);

        // Channel frequency.
        radiotap.extend_from_slice(&frequency.to_le_bytes());

        // Channel flags.
        let channel_flags: u16 = 0;
        radiotap.extend_from_slice(&channel_flags.to_le_bytes());

        // Antenna Signal.
        let sig: i8 = rssi.round().clamp(i8::MIN as f32, i8::MAX as f32) as i8;
        radiotap.push(sig as u8);

        // Antenna. (Sona only uses one antenna)
        radiotap.push(0);

        // RX Flags.
        let rx_flags: u16 = 0;
        radiotap.extend_from_slice(&rx_flags.to_le_bytes());

        // Set final length.
        let rt_len = radiotap.len() as u16;
        radiotap[2..4].copy_from_slice(&rt_len.to_le_bytes());

        radiotap
    }
}

fn send_set_frequency(port: &mut dyn serialport::SerialPort, freq_mhz: u16) -> std::io::Result<()> {
    const MSG_TYPE_CMD: u8 = 2;
    const VERSION: u8 = 1;
    const CMD_SET_FREQUENCY: u8 = 1;

    // Payload: cmd_id + freq_mhz (LE)
    let mut payload = Vec::with_capacity(3);
    payload.push(CMD_SET_FREQUENCY);
    payload.extend_from_slice(&freq_mhz.to_le_bytes());

    // Header: type, version, len (LE)
    let mut msg = Vec::with_capacity(4 + payload.len());
    msg.push(MSG_TYPE_CMD);
    msg.push(VERSION);
    msg.extend_from_slice(&(payload.len() as u16).to_le_bytes());
    msg.extend_from_slice(&payload);

    // COBS frame + delimiter
    let enc = cobs_encode(&msg);
    port.write_all(&enc)?;
    port.write_all(&[0u8])?;
    Ok(())
}

fn reset_reason_to_string(reset_reason: u32) -> String {
    if reset_reason == 0 {
        return "NONE".to_string();
    }

    let mut reasons = Vec::new();

    if reset_reason & 0x00000001 != 0 { reasons.push("PIN_RESET"); }
    if reset_reason & 0x00000002 != 0 { reasons.push("WATCHDOG"); }
    if reset_reason & 0x00000004 != 0 { reasons.push("SOFTWARE_RESET"); }
    if reset_reason & 0x00000008 != 0 { reasons.push("CPU_LOCKUP"); }
    if reset_reason & 0x00010000 != 0 { reasons.push("WAKEUP_GPIO"); }
    if reset_reason & 0x00020000 != 0 { reasons.push("WAKEUP_LPCOMP"); }
    if reset_reason & 0x00040000 != 0 { reasons.push("DEBUG_INTERFACE"); }
    if reset_reason & 0x00080000 != 0 { reasons.push("WAKEUP_NFC"); }
    if reset_reason & 0x00100000 != 0 { reasons.push("WAKEUP_VBUS"); }

    if reasons.is_empty() {
        "UNKNOWN".to_string()
    } else {
        reasons.join(" | ")
    }
}

fn parse_metrics_payload(data: &[u8]) -> Option<SonaMetrics> {
    if data.len() < 20 {
        return None;
    }

    let uptime_ms = u32::from_le_bytes([data[0], data[1], data[2], data[3]]);
    let last_reset_reason = u32::from_le_bytes([data[4], data[5], data[6], data[7]]);
    let temperature_mc = i32::from_le_bytes([data[8], data[9], data[10], data[11]]);
    let frame_queue_used = u32::from_le_bytes([data[12], data[13], data[14], data[15]]);
    let frame_queue_drops = u32::from_le_bytes([data[16], data[17], data[18], data[19]]);

    Some(SonaMetrics {
        uptime_ms,
        last_reset_reason,
        temperature_mc,
        frame_queue_used,
        frame_queue_drops,
    })
}