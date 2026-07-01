use chrono::{DateTime, Local, Utc};
use log::Level;
use serde::Serialize;
use crate::link::payloads::ConfigurationReport;

#[derive(Serialize)]
pub struct DebugFrame {
    pub process_started_at: DateTime<Utc>,
    pub leader_uri: String,
    pub version: String,
    pub configuration: ConfigurationReport,

    pub rpi_temperature: Option<f32>,
    pub memory_total: u64,
    pub memory_free: u64,
    pub cpu_load: f32,
    pub processed_bytes_total: u128,
    pub processed_bytes_avg: u128,

    pub captures: Vec<DebugCaptureInformation>,
    pub logs: Vec<DebugLogMessage>
}

#[derive(Serialize)]
pub struct DebugCaptureInformation {
    pub capture_type: String,
    pub interface_name: String,
    pub is_running: bool,
    pub received: u128,
    pub dropped_buffer: u128,
    pub dropped_interface: u128
}

#[derive(Serialize)]
pub struct DebugLogMessage {
    pub timestamp: DateTime<Local>,
    pub level: String,
    pub message: String
}