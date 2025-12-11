pub const FRAME_TYPE_LIMINA_READINGS: u8 = 0x01;
pub const FRAME_TYPE_MPU_METRICS: u8 = 0x02;

pub const SENSOR_V1_PAYLOAD_SIZE: usize = size_of::<SensorFrameV1>();
pub const SENSOR_V1_FRAME_SIZE: usize = 2 + 1 + 1 + SENSOR_V1_PAYLOAD_SIZE + 2;

pub const MPU_METRICS_V1_PAYLOAD_SIZE: usize = size_of::<MpuMetricsFrameV1>();
pub const MPU_METRICS_V1_FRAME_SIZE: usize = 2 + 1 + 1 + MPU_METRICS_V1_PAYLOAD_SIZE + 2;

#[derive(Debug)]
pub struct SensorFrameV1 {
    pub millis: u32, // This can safely wrap.
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
        if buf.len() != SENSOR_V1_PAYLOAD_SIZE {
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

#[derive(Debug)]
#[repr(C, packed)]
pub struct MpuMetricsFrameV1 {
    pub core_temp_c: f32,
    pub vdda_mv: u16,
    pub bme280_error_count: u32,
    pub vl53_error_count: u32,
    pub lis3dh_error_count: u32,
    pub i2c1_error_count: u32,
}

impl MpuMetricsFrameV1 {
    pub fn from_bytes(buf: &[u8]) -> Option<Self> {
        if buf.len() != MPU_METRICS_V1_PAYLOAD_SIZE {
            return None;
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

        fn read_u32(idx: &mut usize, buf: &[u8]) -> u32 {
            let v = u32::from_le_bytes(buf[*idx..*idx + 4].try_into().unwrap());
            *idx += 4;
            v
        }

        let mut idx = 0;

        let core_temp_c = read_f32(&mut idx, buf);
        let vdda_mv = read_u16(&mut idx, buf);
        let bme280_error_count = read_u32(&mut idx, buf);
        let vl53_error_count = read_u32(&mut idx, buf);
        let lis3dh_error_count = read_u32(&mut idx, buf);
        let i2c1_error_count = read_u32(&mut idx, buf);

        Some(Self {
            core_temp_c,
            vdda_mv,
            bme280_error_count,
            vl53_error_count,
            lis3dh_error_count,
            i2c1_error_count,
        })
    }
}