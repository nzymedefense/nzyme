use std::sync::{Arc, Mutex};
use std::thread;
use std::thread::sleep;
use std::time::Duration;
use log::{debug, error, info, warn};
use crate::metrics::Metrics;
use crate::peripherals::limina::sensors::frames::{
    SensorFrameV1,
    MpuMetricsFrameV1,
    FRAME_TYPE_LIMINA_READINGS,
    FRAME_TYPE_MPU_METRICS,
    SENSOR_V1_FRAME_SIZE,
    SENSOR_V1_PAYLOAD_SIZE,
    MPU_METRICS_V1_FRAME_SIZE,
    MPU_METRICS_V1_PAYLOAD_SIZE,
};

use crate::usb::usb::find_first_nzyme_usb_device_with_device_id;

const MAGIC: &[u8; 2] = b"NZ";
const VERSION: u8 = 0x01;

const LIMINA_1_PID: u16 = 0x0200;

#[derive(Debug)]
enum ParsedFrame {
    Limina(SensorFrameV1),
    MpuMetrics(MpuMetricsFrameV1),
}

pub struct Limina {
    metrics: Arc<Mutex<Metrics>>
}

impl Limina {

    pub fn new(metrics: Arc<Mutex<Metrics>>) -> Self {
        Self { metrics }
    }

    pub fn launch_monitor(&self) {
        let metrics = self.metrics.clone();
        thread::spawn(move || {
            let mut read_buf = [0u8; 256];
            let mut buffer: Vec<u8> = Vec::with_capacity(1024);

            info!("Limina initialized and running.");

            loop {
                sleep(Duration::from_millis(5000));

                info!("Attempting to detect Limina.");

                let device = match find_first_nzyme_usb_device_with_device_id(LIMINA_1_PID) {
                    Ok(device) => {
                        match device {
                            Some(device) => device,
                            None => {
                                warn!("No Limina device discovered. Expected PID [0x{:04X}].", LIMINA_1_PID);
                                continue;
                            }
                        }
                    },
                    Err(e) => {
                        error!("Could not detect USB devices: {}", e);
                        continue;
                    }
                };

                info!("Limina detected: [{:?}]", device);

                let acm_port = match device.acm_port {
                    Some(acm_port) => acm_port,
                    None => {
                        error!("Limina at {}:{} does not expose a ACM port.", device.bus, device.address);
                        continue;
                    }
                };

                let mut port = serialport::new(&acm_port, 115_200)
                    .timeout(Duration::from_millis(1000))
                    .open()
                    .expect("Failed to open serial port");

                loop {
                    match port.read(&mut read_buf) {
                        Ok(n) if n > 0 => {
                            buffer.extend_from_slice(&read_buf[..n]);

                            if let Some(parsed) = Self::process_buffer(&mut buffer) {
                                match parsed {
                                    ParsedFrame::Limina(frame) => {
                                        info!("Limina Frame: {:?}", frame);

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
                                            }
                                            Err(e) => error!("Could not acquire metrics mutex: {}", e),
                                        }
                                    }

                                    ParsedFrame::MpuMetrics(frame) => {
                                        info!("Limina MCU metrics: {:?}", frame);

                                        match metrics.lock() {
                                            Ok(mut metrics) => {
                                                metrics.set_gauge_float(
                                                    "limina.core_temperature_c",
                                                    frame.core_temp_c,
                                                );
                                                metrics.set_gauge_float(
                                                    "limina.vdda_mv",
                                                    frame.vdda_mv as f32,
                                                );
                                                metrics.set_gauge_float(
                                                    "limina.environment_error_count",
                                                    frame.bme280_error_count as f32,
                                                );
                                                metrics.set_gauge_float(
                                                    "limina.tof_error_count",
                                                    frame.vl53_error_count as f32,
                                                );
                                                metrics.set_gauge_float(
                                                    "limina.accel_error_count",
                                                    frame.lis3dh_error_count as f32,
                                                );
                                                metrics.set_gauge_float(
                                                    "limina.i2c1_error_count",
                                                    frame.i2c1_error_count as f32,
                                                );
                                            }
                                            Err(e) => error!("Could not acquire metrics mutex: {}", e),
                                        }
                                    }
                                }
                            }

                        }
                        Ok(_) => {
                            // Nothing read, continue.
                        }
                        Err(e) => {
                            // We ignore time out errors that happen sometimes.
                            if e.kind() != std::io::ErrorKind::TimedOut {
                                // Exit on any other error. Likely a disconnected device.
                                error!("Serial read error: {:?}", e);
                                sleep(Duration::from_millis(1000));
                                break;
                            }
                        }
                    }
                }

                error!("Limina read loop exited. Restarting.");
            }
        });
    }

    fn process_buffer(buf: &mut Vec<u8>) -> Option<ParsedFrame> {
        loop {
            // Find magic bytes.
            let start_opt = buf.windows(2).position(|w| w == MAGIC);

            let start = match start_opt {
                Some(s) => s,
                None => {
                    buf.clear();
                    return None;
                }
            };

            // Ensure we have header (magic + version + type).
            if buf.len() < start + 4 {
                if start > 0 {
                    buf.drain(0..start);
                }
                return None;
            }

            let version = buf[start + 2];
            if version != VERSION {
                error!("Version mismatch: got {}, expected {}", version, VERSION);
                buf.drain(0..start + 1);
                continue;
            }

            let frame_type = buf[start + 3];

            // Per-type frame size.
            let expected_frame_size = match frame_type {
                FRAME_TYPE_LIMINA_READINGS => SENSOR_V1_FRAME_SIZE,
                FRAME_TYPE_MPU_METRICS => MPU_METRICS_V1_FRAME_SIZE,
                other => {
                    warn!("Ignoring frame with unknown type: 0x{:02X}", other);
                    buf.drain(0..start + 1);
                    continue;
                }
            };

            // Wait until full frame available.
            if buf.len() < start + expected_frame_size {
                if start > 0 {
                    buf.drain(0..start);
                }
                return None;
            }

            let frame = &buf[start..start + expected_frame_size];

            match frame_type {
                FRAME_TYPE_LIMINA_READINGS => {
                    // CRC.
                    let crc_region_start = 2;
                    let crc_region_end = crc_region_start + 2 + SENSOR_V1_PAYLOAD_SIZE;
                    let crc_region = &frame[crc_region_start..crc_region_end];

                    let crc_expected = Self::crc16_ccitt(crc_region);
                    let crc_bytes = &frame[crc_region_end..crc_region_end + 2];
                    let crc_received = u16::from_le_bytes([crc_bytes[0], crc_bytes[1]]);

                    if crc_expected != crc_received {
                        debug!("CRC mismatch (limina): expected 0x{:04X}, got 0x{:04X}",
                            crc_expected, crc_received
                    );
                        buf.drain(0..start + 1);
                        continue;
                    }

                    let payload_start = 4;
                    let payload_end = payload_start + SENSOR_V1_PAYLOAD_SIZE;
                    let payload = &frame[payload_start..payload_end];

                    match SensorFrameV1::from_bytes(payload) {
                        Some(f) => {
                            buf.drain(0..start + expected_frame_size);
                            return Some(ParsedFrame::Limina(f));
                        }
                        None => {
                            error!("Failed to parse Limina frame.");
                            buf.drain(0..start + expected_frame_size);
                            return None;
                        }
                    }
                }

                FRAME_TYPE_MPU_METRICS => {
                    // CRC.
                    let crc_region_start = 2;
                    let crc_region_end = crc_region_start + 2 + MPU_METRICS_V1_PAYLOAD_SIZE;
                    let crc_region = &frame[crc_region_start..crc_region_end];

                    let crc_expected = Self::crc16_ccitt(crc_region);
                    let crc_bytes = &frame[crc_region_end..crc_region_end + 2];
                    let crc_received = u16::from_le_bytes([crc_bytes[0], crc_bytes[1]]);

                    if crc_expected != crc_received {
                        debug!("CRC mismatch (mpu): expected 0x{:04X}, got 0x{:04X}",
                            crc_expected, crc_received);
                        buf.drain(0..start + 1);
                        continue;
                    }

                    let payload_start = 4;
                    let payload_end = payload_start + MPU_METRICS_V1_PAYLOAD_SIZE;
                    let payload = &frame[payload_start..payload_end];

                    match MpuMetricsFrameV1::from_bytes(payload) {
                        Some(f) => {
                            buf.drain(0..start + expected_frame_size);
                            return Some(ParsedFrame::MpuMetrics(f));
                        }
                        None => {
                            error!("Failed to parse MPU metrics frame.");
                            buf.drain(0..start + expected_frame_size);
                            return None;
                        }
                    }
                }

                _ => unreachable!(),
            }
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