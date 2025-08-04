use chrono::{DateTime, Utc};

#[derive(Debug)]
pub struct NMEAMessage {
    pub interface: String,
    pub timestamp: DateTime<Utc>,
    pub sentence: String
}