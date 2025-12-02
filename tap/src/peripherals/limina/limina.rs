use std::sync::{Arc, Mutex};
use std::thread;
use std::thread::sleep;
use std::time::Duration;
use log::{debug, error, info, warn};
use crate::metrics::Metrics;
use crate::peripherals::limina::sensors::frames::{SensorFrameV1, V1_FRAME_SIZE, V1_PAYLOAD_SIZE};
use crate::usb::usb::find_first_nzyme_usb_device_with_device_id;

const MAGIC: &[u8; 2] = b"NZ";
const VERSION: u8 = 0x01;

const LIMINA_1_PID: u16 = 0x0200;

pub struct Limina {
    metrics: Arc<Mutex<Metrics>>
}

impl Limina {

    pub fn new(metrics: Arc<Mutex<Metrics>>) -> Self {
        Self { metrics }
    }

    pub fn launch_monitor(&self) {
        info!("Attempting to detect Limina.");

        let device = match find_first_nzyme_usb_device_with_device_id(LIMINA_1_PID) {
            Ok(device) => {
                match device {
                    Some(device) => device,
                    None => {
                        warn!("No Limina device discovered. Expected PID [0x{:04X}].", LIMINA_1_PID);
                        return;
                    }
                }
            },
            Err(e) => {
                error!("Could not detect USB devices: {}", e);
                return;
            }
        };

        info!("Limina detected: [{:?}]", device);

        let acm_port = match device.acm_port {
            Some(acm_port) => acm_port,
            None => {
                error!("Limina at {}:{} does not expose a ACM port.", device.bus, device.address);
                return;
            }
        };

        let mut port = serialport::new(acm_port, 115_200)
            .timeout(Duration::from_millis(1000))
            .open()
            .expect("Failed to open serial port");

        let metrics = self.metrics.clone();
        thread::spawn(move || {
            let mut read_buf = [0u8; 256];
            let mut buffer: Vec<u8> = Vec::with_capacity(1024);

            info!("Limina initialized and running.");

            loop {
                match port.read(&mut read_buf) {
                    Ok(n) if n > 0 => {
                        buffer.extend_from_slice(&read_buf[..n]);

                        if let Some(frame) = Self::process_buffer(&mut buffer) {
                            // We have a complete frame.
                            match metrics.lock() {
                                Ok(mut metrics) => {
                                    metrics.set_gauge_float(
                                        "limina.temperature_1_c", frame.temperature_1_c
                                    );

                                    metrics.set_gauge_float(
                                        "limina.pressure_1_hpa", frame.pressure_1_hpa
                                    );

                                    metrics.set_gauge_float(
                                        "limina.humidity_1_pct", frame.humidity_1_pct
                                    );

                                    metrics.set_gauge_float(
                                        "limina.accel_1_mag_mean", frame.accel_1_mag_mean
                                    );

                                    metrics.set_gauge_float(
                                        "limina.accel_1_baseline", frame.accel_1_baseline
                                    );
                                },
                                Err(e) => error!("Could not acquire metrics mutex: {}", e)
                            }
                        }
                    }
                    Ok(_) => {
                        // Nothing read, continue.
                    }
                    Err(e) => {
                        if e.kind() != std::io::ErrorKind::TimedOut {
                            error!("Serial read error: {:?}", e);
                            sleep(Duration::from_millis(1000));
                        }
                    }
                }
            }
        });
    }

    fn process_buffer(buf: &mut Vec<u8>) -> Option<SensorFrameV1> {
        loop {
            // Find magic bytes.
            let start_opt = buf
                .windows(2)
                .position(|w| w == MAGIC);

            let start = match start_opt {
                Some(s) => s,
                None => {
                    // No magic bytes left. clear buffer to avoid unbounded growth.
                    buf.clear();

                    return None;
                }
            };

            // If we don't yet have a full frame after the magic, wait for more data.
            if buf.len() < start + V1_FRAME_SIZE {
                // Drop garbage before the magic if any.
                if start > 0 {
                    buf.drain(0..start);
                }

                return None;
            }

            // We have at least one full frame.
            let frame = &buf[start..start + V1_FRAME_SIZE];

            // Check version.
            let version = frame[2];
            if version != VERSION {
                error!("Version mismatch: got {}, expected {}", version, VERSION);
                // Drop first byte and resync.
                buf.drain(0..start + 1);
                continue;
            }

            // CRC.
            let crc_region_start = 2;
            let crc_region_end = crc_region_start + 1 + V1_PAYLOAD_SIZE;
            let crc_region = &frame[crc_region_start..crc_region_end];

            let crc_expected = Self::crc16_ccitt(crc_region);
            let crc_bytes = &frame[crc_region_end..crc_region_end + 2];
            let crc_received = u16::from_le_bytes([crc_bytes[0], crc_bytes[1]]);

            if crc_expected != crc_received {
                debug!("CRC mismatch: expected 0x{:04X}, got 0x{:04X}", crc_expected, crc_received);
                // Drop first byte and resync.
                buf.drain(0..start + 1);
                continue;
            }

            // CRC OK, parse payload.
            let payload_start = 3; // after magic (2) + version (1)
            let payload_end = payload_start + V1_PAYLOAD_SIZE;
            let payload = &frame[payload_start..payload_end];


            match SensorFrameV1::from_bytes(payload) {
                Some(f) => {
                    // Done! Frame complete.

                    // Remove everything up to and including this frame from the buffer.
                    buf.drain(0..start + V1_FRAME_SIZE);

                    return Some(f)
                },
                None => {
                    error!("Failed to parse Limina frame.");
                    return None;
                }
            };
        }

    }

    fn crc16_ccitt(data: &[u8]) -> u16 {
        let mut crc: u16 = 0xFFFF;

        for &byte in data {
            crc ^= (byte as u16) << 8;
            for _ in 0..8 {
                if (crc & 0x8000) != 0 {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
        }

        crc
    }

}