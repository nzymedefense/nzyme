use std::thread::sleep;
use std::time::Duration;
use log::{debug, error, info};

const MAGIC: &[u8; 2] = b"NZ";
const VERSION: u8 = 0x01;

const PAYLOAD_SIZE: usize = 52;
const FRAME_SIZE: usize = 2 + 1 + PAYLOAD_SIZE + 2;

#[derive(Debug)]
struct SensorFrameV1 {
    millis: u32,
    temperature_1_c: f32,
    pressure_1_hpa: f32,
    humidity_1_pct: f32,
    distance_1_mm: f32,
    accel_1_x_g: f32,
    accel_1_y_g: f32,
    accel_1_z_g: f32,
    accel_1_mag_mean: f32,
    accel_1_delta_rms: f32,
    accel_1_delta_max: f32,
    accel_1_delta_hits: u16,
    accel_1_baseline: f32,
}

impl SensorFrameV1 {
    fn from_bytes(buf: &[u8]) -> Option<Self> {
        if buf.len() != PAYLOAD_SIZE {
            return None;
        }

        fn read_u32(idx: &mut usize, buf: &[u8]) -> u32 {
            let v = u32::from_le_bytes(buf[*idx..*idx + 4].try_into().unwrap());
            *idx += 4;
            v
        }

        fn read_f32(idx: &mut usize, buf: &[u8]) -> f32 {
            let v = f32::from_le_bytes(buf[*idx..*idx + 4].try_into().unwrap());
            *idx += 4;
            v
        }

        fn read_u16(idx: &mut usize, buf: &[u8]) -> u16 {
            let v = u16::from_le_bytes(buf[*idx..*idx + 2].try_into().unwrap());
            *idx += 2;
            v
        }

        let mut idx = 0;

        let millis        = read_u32(&mut idx, buf);
        let temperature_1_c = read_f32(&mut idx, buf);
        let pressure_1_hpa  = read_f32(&mut idx, buf);
        let humidity_1_pct  = read_f32(&mut idx, buf);
        let distance_1_mm   = read_f32(&mut idx, buf);
        let accel_1_x_g     = read_f32(&mut idx, buf);
        let accel_1_y_g     = read_f32(&mut idx, buf);
        let accel_1_z_g     = read_f32(&mut idx, buf);
        let accel_1_mag_mean      = read_f32(&mut idx, buf);
        let accel_1_delta_rms     = read_f32(&mut idx, buf);
        let accel_1_delta_max     = read_f32(&mut idx, buf);
        let accel_1_delta_hits    = read_u16(&mut idx, buf);

        // Padding between delta_hits (u16) and baseline (f32) on STM32
        idx += 2; // skip 2 padding bytes

        let accel_1_baseline = read_f32(&mut idx, buf);

        Some(Self {
            millis,
            temperature_1_c,
            pressure_1_hpa,
            humidity_1_pct,
            distance_1_mm,
            accel_1_x_g,
            accel_1_y_g,
            accel_1_z_g,
            accel_1_mag_mean,
            accel_1_delta_rms,
            accel_1_delta_max,
            accel_1_delta_hits,
            accel_1_baseline,
        })
    }
}


pub fn read_loop() {
    let port_name = "/dev/ttyACM2"; // TODO auto-detect

    let mut port = serialport::new(port_name, 115_200)
        .timeout(Duration::from_millis(1000))
        .open()
        .expect("Failed to open serial port");

    let mut read_buf = [0u8; 256];
    let mut buffer: Vec<u8> = Vec::with_capacity(1024);

    loop {
        match port.read(&mut read_buf) {
            Ok(n) if n > 0 => {
                buffer.extend_from_slice(&read_buf[..n]);
                process_buffer(&mut buffer);
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
}

fn process_buffer(buf: &mut Vec<u8>) {
    loop {
        // Find magic bytes.
        let start_opt = buf
            .windows(2)
            .position(|w| w == MAGIC);

        let start = match start_opt {
            Some(s) => s,
            None => {
                // No magic bytes left; clear buffer to avoid unbounded growth.
                buf.clear();
                return;
            }
        };

        // If we don't yet have a full frame after the magic, wait for more data.
        if buf.len() < start + FRAME_SIZE {
            // Drop garbage before the magic if any.
            if start > 0 {
                buf.drain(0..start);
            }
            return;
        }

        // We have at least one full frame starting at `start`.
        let frame = &buf[start..start + FRAME_SIZE];

        // Check version.
        let version = frame[2];
        if version != VERSION {
            error!("Version mismatch: got {}, expected {}", version, VERSION);
            // Drop first byte and resync
            buf.drain(0..start + 1);
            continue;
        }

        // CRC.
        let crc_region_start = 2;
        let crc_region_end = crc_region_start + 1 + PAYLOAD_SIZE; // 2 + 1 + 50 = 53 (exclusive)
        let crc_region = &frame[crc_region_start..crc_region_end];

        let crc_expected = crc16_ccitt(crc_region);
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
        let payload_end = payload_start + PAYLOAD_SIZE;
        let payload = &frame[payload_start..payload_end];

        if let Some(f) = SensorFrameV1::from_bytes(payload) {
            info!(
                "t={:10} ms | T={:6.2} °C | P={:7.2} hPa | H={:5.1} % | d={:7.1} mm | \
                 ax={:6.3} g | ay={:6.3} g | az={:6.3} g | \
                 mag_mean={:7.3} | delta_rms={:7.3} | delta_max={:7.3} | \
                 hits={:5} | baseline={:7.3}",
                f.millis,
                f.temperature_1_c,
                f.pressure_1_hpa,
                f.humidity_1_pct,
                f.distance_1_mm,
                f.accel_1_x_g,
                f.accel_1_y_g,
                f.accel_1_z_g,
                f.accel_1_mag_mean,
                f.accel_1_delta_rms,
                f.accel_1_delta_max,
                f.accel_1_delta_hits,
                f.accel_1_baseline,
            );
        } else {
            error!("Failed to parse payload bytes.");
        }

        // Remove everything up to and including this frame from the buffer.
        buf.drain(0..start + FRAME_SIZE);
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
