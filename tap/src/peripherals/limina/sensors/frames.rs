pub const V1_PAYLOAD_SIZE: usize = 52;
pub const V1_FRAME_SIZE: usize = 2 + 1 + V1_PAYLOAD_SIZE + 2;

#[derive(Debug)]
pub struct SensorFrameV1 {
    pub millis: u32,
    pub temperature_1_c: f32,
    pub pressure_1_hpa: f32,
    pub humidity_1_pct: f32,
    pub distance_1_mm: f32,
    pub accel_1_x_g: f32,
    pub accel_1_y_g: f32,
    pub accel_1_z_g: f32,
    pub accel_1_mag_mean: f32,
    pub accel_1_delta_rms: f32,
    pub accel_1_delta_max: f32,
    pub accel_1_delta_hits: u16,
    pub accel_1_baseline: f32,
}

impl SensorFrameV1 {
    pub fn from_bytes(buf: &[u8]) -> Option<Self> {
        if buf.len() != V1_PAYLOAD_SIZE {
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

        let millis = read_u32(&mut idx, buf);
        let temperature_1_c = read_f32(&mut idx, buf);
        let pressure_1_hpa = read_f32(&mut idx, buf);
        let humidity_1_pct = read_f32(&mut idx, buf);
        let distance_1_mm = read_f32(&mut idx, buf);
        let accel_1_x_g = read_f32(&mut idx, buf);
        let accel_1_y_g = read_f32(&mut idx, buf);
        let accel_1_z_g = read_f32(&mut idx, buf);
        let accel_1_mag_mean = read_f32(&mut idx, buf);
        let accel_1_delta_rms = read_f32(&mut idx, buf);
        let accel_1_delta_max = read_f32(&mut idx, buf);
        let accel_1_delta_hits = read_u16(&mut idx, buf);

        // Skip 2 padding bytes.
        idx += 2;

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