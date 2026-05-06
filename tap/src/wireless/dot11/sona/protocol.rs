use std::sync::{Arc, Mutex};
use log::{debug, error, trace, warn};
use crate::messagebus::bus::Bus;
use crate::metrics::Metrics;
use crate::peripherals::cobs::cobs_encode;
use crate::peripherals::crc::crc16_ccitt;
use crate::wireless::dot11::frames::Dot11CaptureSource::Acquisition;
use crate::wireless::dot11::frames::Dot11RawFrame;
use crate::wireless::dot11::sona::radiotap::create_radiotap_header;
use crate::wireless::dot11::sona::sona_frame_header::SonaFrameHeader;
use crate::wireless::dot11::sona::sona_metrics::{parse_metrics_payload, METRICS_PAYLOAD_BYTES};
use crate::wireless::dot11::sona::uptime_offset::UptimeOffset;

const WIRE_PROTOCOL_VERSION: u8 = 2;

const MSG_TYPE_DOT11: u8 = 1;
const MSG_TYPE_CMD: u8 = 2;
const MSG_TYPE_METRICS: u8 = 3;

const CMD_SET_FREQUENCY: u8 = 1;

const HEADER_BYTES: usize = 6;

pub fn process_decoded(interface_name: &str,
                       decoded: &[u8],
                       uptime_offset: &mut Option<UptimeOffset>,
                       metrics: Arc<Mutex<Metrics>>,
                       bus: Arc<Bus>) {
    if decoded.len() < HEADER_BYTES {
        trace!("Dropping frame too short for header: <{}> bytes.", decoded.len());
        return;
    }

    let msg_type = decoded[0];
    let version = decoded[1];
    let payload_len = u16::from_le_bytes([decoded[2], decoded[3]]) as usize;
    let crc_in_hdr = u16::from_le_bytes([decoded[4], decoded[5]]);

    if version != WIRE_PROTOCOL_VERSION {
        trace!("Unknown wire protocol version: {} (expected {})", version, WIRE_PROTOCOL_VERSION);
        return;
    }

    if decoded.len() != HEADER_BYTES + payload_len {
        trace!("Frame length mismatch: declared {}, got {}.", HEADER_BYTES + payload_len, decoded.len());
        return;
    }

    let payload = &decoded[HEADER_BYTES..HEADER_BYTES + payload_len];

    let computed_crc = crc16_ccitt(payload);
    if computed_crc != crc_in_hdr {
        trace!("CRC mismatch: declared 0x{:04x}, computed 0x{:04x}.", crc_in_hdr, computed_crc);
        return;
    }

    handle_payload(interface_name, msg_type, payload, uptime_offset, metrics, bus);
}

fn handle_payload(interface_name: &str,
                  msg_type: u8,
                  payload: &[u8],
                  uptime_offset: &mut Option<UptimeOffset>,
                  metrics: Arc<Mutex<Metrics>>,
                  bus: Arc<Bus>) {
    match msg_type {
        MSG_TYPE_DOT11 => {
            if payload.len() < SonaFrameHeader::BYTES {
                error!("Dropping 802.11 frame that is too short to fit Sona header: <{}> bytes.",
                        payload.len());
                return;
            }

            let header_bytes = &payload[..SonaFrameHeader::BYTES];
            let hdr = match SonaFrameHeader::parse(header_bytes) {
                Some(h) => h,
                None => {
                    warn!("Could not parse frame header: [{:?}]", header_bytes);
                    return;
                }
            };

            let rssi = match hdr.rssi_dbm {
                Some(r) => r,
                None => {
                    trace!("Skipping Sona frame with no RSSI.");
                    return;
                }
            };

            /*
             * Anchor or apply firmware-uptime to host-wallclock offset.
             * The first frame of a session establishes it; thereafter we
             * use it to stamp every frame.
             */
            let offset = uptime_offset
                .get_or_insert_with(|| UptimeOffset::new(hdr.capture_uptime_ms));
            let wall_micros = offset.wall_micros(hdr.capture_uptime_ms);

            let frame_bytes = &payload[SonaFrameHeader::BYTES..];

            /*
             * Radiotap TSFT is microseconds in the device's local time
             * domain. Use firmware uptime directly so anyone parsing the
             * radiotap sees a monotonic per-device timeline. wall_micros
             * above is the corrected host-side value.
             */
            let tsft_us = (hdr.capture_uptime_ms as u64) * 1000;

            let radiotap = create_radiotap_header(
                hdr.rate_flags,
                hdr.rate_code,
                hdr.freq_mhz,
                rssi,
                tsft_us,
            );

            let mut full_frame = Vec::with_capacity(frame_bytes.len() + radiotap.len());
            full_frame.extend_from_slice(&radiotap);
            full_frame.extend_from_slice(frame_bytes);
            let full_frame_length = full_frame.len();

            let data = Dot11RawFrame {
                capture_source: Acquisition,
                interface_name: interface_name.to_string(),
                data: full_frame,
            };

            trace!("Sona frame on {} captured at uptime {}ms => host walltime {}us",
                    interface_name, hdr.capture_uptime_ms, wall_micros);

            match metrics.lock() {
                Ok(mut metrics) => {
                    metrics.increment_processed_bytes_total(full_frame_length as u32);
                    metrics.update_capture(interface_name, true, 0, 0);
                }
                Err(e) => error!("Could not acquire metrics mutex: {}", e),
            }

            match bus.dot11_broker.sender.lock() {
                Ok(mut sender) => sender.send_packet(Arc::new(data), full_frame_length as u32),
                Err(e) => {
                    error!("Could not acquire 802.11 handler broker mutex: {}", e)
                }
            }
        }
        MSG_TYPE_METRICS => {
            if let Some(sona_metrics) = parse_metrics_payload(payload) {
                debug!("Sona [{}] metrics frame: [{}]", interface_name, sona_metrics);
                match metrics.lock() {
                    Ok(mut metrics) => {
                        metrics.update_capture(
                            interface_name, true, sona_metrics.frame_queue_drops, 0,
                        );
                        metrics.set_gauge_float(
                            &format!("{}.temperature_mc", interface_name),
                            sona_metrics.temperature_mc as f32 / 1000.0,
                        );
                        metrics.set_gauge(
                            &format!("{}.frame_queue_drops", interface_name),
                            sona_metrics.frame_queue_drops as i128,
                        );
                        metrics.set_gauge(
                            &format!("{}.frame_queue_stale_drops", interface_name),
                            sona_metrics.frame_queue_stale_drops as i128,
                        );
                        metrics.set_gauge(
                            &format!("{}.frame_queue_used", interface_name),
                            sona_metrics.frame_queue_used as i128,
                        );
                    }
                    Err(e) => error!("Could not acquire metrics mutex: {}", e),
                }
            } else {
                warn!("Failed to parse metrics payload: <{}> bytes (expected {}).",
                        payload.len(), METRICS_PAYLOAD_BYTES);
            }
        }
        _ => {
            debug!("Unknown message type: {}", msg_type);
        }
    }
}

pub fn send_set_frequency(port: &mut dyn serialport::SerialPort, freq_mhz: u16) -> std::io::Result<()> {
    // Payload: cmd_id + freq_mhz (LE).
    let payload = [
        CMD_SET_FREQUENCY,
        (freq_mhz & 0xFF) as u8,
        (freq_mhz >> 8) as u8,
    ];

    let crc = crc16_ccitt(&payload);

    // Header: type(1), version(1), len(2 LE), crc(2 LE).
    let mut msg = Vec::with_capacity(HEADER_BYTES + payload.len());
    msg.push(MSG_TYPE_CMD);
    msg.push(WIRE_PROTOCOL_VERSION);
    msg.extend_from_slice(&(payload.len() as u16).to_le_bytes());
    msg.extend_from_slice(&crc.to_le_bytes());
    msg.extend_from_slice(&payload);

    let mut enc = cobs_encode(&msg);
    enc.push(0u8);
    port.write_all(&enc)?;
    Ok(())
}