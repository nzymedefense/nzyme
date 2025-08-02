use chrono::{DateTime, Utc};
use crate::wireless::positioning::gnss::gnss_constellation::GNSSConstellation;

#[derive(Debug)]
pub struct GPGGASentence {
    pub constellation: GNSSConstellation,
    pub time: DateTime<Utc>,
    pub latitude: f64,
    pub longitude: f64,
    pub fix_quality: FixQuality,
    pub num_satellites: u8,
    pub hdop: f32,
    pub altitude_m: f32,
    pub geoid_separation_m: f32,
}

#[derive(Debug)]
pub struct GPGSASentence {
    pub constellation: GNSSConstellation,
    pub mode: SelectionMode,
    pub fix: FixType,
    pub fix_satellites: Vec<u8>,
    pub pdop: f32,
    pub hdop: f32,
    pub vdop: f32
}

#[derive(Debug)]
pub struct GPGSVSentence {
    pub constellation: GNSSConstellation,
    pub total_messages: u8,
    pub message_number: u8,
    pub satellites_in_view: u8,
    pub satellites: Vec<SatelliteInfo>,
}

#[derive(Debug)]
pub enum FixQuality {
    Invalid,
    GPS,
    DGPS,
    PPS,
    RTK,
    FloatRTK,
    Estimated,
    ManualInput,
    Simulation
}

#[derive(Debug)]
pub enum SelectionMode {
    Automatic,
    Manual
}

#[derive(Debug)]
pub enum FixType {
    NoFix,
    Fix2D,
    Fix3D
}

#[derive(Debug)]
pub struct SatelliteInfo {
    pub prn: u8,
    pub elevation_degrees: u8,
    pub azimuth_degrees: u16,
    pub snr_db: Option<u8>,
}