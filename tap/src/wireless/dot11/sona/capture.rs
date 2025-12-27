use std::sync::{Arc, Mutex};
use std::thread::sleep;
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

pub struct Capture {
    pub metrics: Arc<Mutex<Metrics>>,
    pub bus: Arc<Bus>,
    pub command_receiver: Receiver<SonaCommand>,
}

impl Capture {
    pub fn run(&self, serial: &str) {
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

            Ok(port) => port,
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

                    while let Some(delimiter_pos) = acc[processed_up_to..]
                        .iter().position(|&b| b == 0) {

                        let frame_end = processed_up_to + delimiter_pos;

                        if frame_end > processed_up_to {
                            // Non-empty frame
                            let encoded = &acc[processed_up_to..frame_end];

                            match cobs_decode(encoded) {
                                Ok(decoded) => {
                                    if decoded.len() < SonaFrameHeader::BYTES {
                                        error!("Dropping frame that is too short to fit Sona \
                                            header: <{}> bytes.", decoded.len());
                                    } else {
                                        // Parse frame header.
                                        let header_bytes = &decoded[..SonaFrameHeader::BYTES];
                                        if let Some(hdr) = SonaFrameHeader::parse(header_bytes) {
                                            /* We should always have an RSSI, but, if not, skip
                                             * the frame.
                                             */
                                            if let Some(rssi) = hdr.rssi_dbm {
                                                let frame = &decoded[SonaFrameHeader::BYTES..];

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

                                                // Write to 802.11 pipeline.
                                                match self.bus.dot11_broker.sender.lock() {
                                                    Ok(mut sender) => {
                                                        sender.send_packet(
                                                            Arc::new(data),
                                                            full_frame_length as u32
                                                        )
                                                    },
                                                    Err(e) => {
                                                        error!("Could not acquire 802.11 handler \
                                                            broker mutex: {}", e)
                                                    }
                                                }
                                            } else {
                                                trace!("Skipping Sona frame with no RSSI.");
                                            }
                                        } else {
                                            warn!("Could not parse frame header: [{:?}]",
                                                header_bytes);
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