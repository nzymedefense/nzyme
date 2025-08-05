use chrono::{DateTime, Utc};
use strum_macros::Display;
use crate::wireless::positioning::gnss::gnss_constellation::GNSSConstellation;

#[derive(Debug)]
pub struct GPGGASentence {
    pub constellation: GNSSConstellation,
    pub time: Option<DateTime<Utc>>,
    pub timestamp: DateTime<Utc>,
    pub latitude: Option<f64>,
    pub longitude: Option<f64>,
    pub num_satellites: Option<u8>,
    pub altitude_m: Option<f32>,
    pub geoid_separation_m: Option<f32>
}

#[derive(Debug)]
pub struct GPGSASentence {
    pub constellation: GNSSConstellation,
    pub fix: FixType,
    pub fix_satellites: Vec<u8>,
    pub pdop: Option<f32>,
    pub hdop: Option<f32>,
    pub vdop: Option<f32>
}

#[derive(Debug)]
pub struct GPGSVSentence {
    pub constellation: GNSSConstellation,
    pub total_messages: Option<u8>,
    pub message_number: Option<u8>,
    pub satellites_in_view: Option<u8>,
    pub satellites: Vec<SatelliteInfo>,
}

#[derive(Debug, Display, Eq, PartialEq, Hash)]
pub enum FixType {
    NoFix,
    Fix2D,
    Fix3D
}

#[derive(Debug)]
pub struct SatelliteInfo {
    pub prn: u8,
    pub elevation_degrees: Option<u8>,
    pub azimuth_degrees: Option<u16>,
    pub snr_db: Option<u8>,
}