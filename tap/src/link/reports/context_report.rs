use chrono::{DateTime, Utc};
use serde::Serialize;

#[derive(Serialize)]
pub struct ContextReport {
    pub macs: Vec<MacContextReport>
}

#[derive(Serialize)]
pub struct MacContextReport {
    pub mac: String,
    pub ip_addresses: Vec<ContextDataReport>,
    pub hostnames: Vec<ContextDataReport>,
}

#[derive(Serialize)]
pub struct ContextDataReport {
    pub value: String,
    pub source: String,
    pub last_seen: DateTime<Utc>
}