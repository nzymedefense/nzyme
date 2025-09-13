use chrono::{DateTime, Utc};

#[derive(Debug)]
pub struct NMEAMessage {
    pub interface: String,
    pub timestamp: DateTime<Utc>,
    pub sentence: String,
    pub offset_lat: Option<f64>,
    pub offset_lon: Option<f64>
}