use std::collections::{HashMap, HashSet};
use std::sync::MutexGuard;
use chrono::{DateTime, Utc};
use serde::Serialize;
use crate::state::tables::ntp_table::NtpTransaction;

#[derive(Serialize)]
pub struct NtpTransactionsReport {
    transactions: Vec<NtpTransactionReport>
}

#[derive(Serialize)]
pub struct NtpTransactionReport {
    pub transaction_key: String,
    pub transaction_type: String,
    pub complete: bool,
    pub notes: HashSet<String>,
    pub client_mac: Option<String>,
    pub server_mac: Option<String>,
    pub client_address: String,
    pub server_address: String,
    pub client_port: u16,
    pub server_port: u16,
    pub request_size: Option<u32>,
    pub response_size: Option<u32>,
    pub timestamp_client_transmit: Option<DateTime<Utc>>,
    pub timestamp_server_receive: Option<DateTime<Utc>>,
    pub timestamp_server_transmit: Option<DateTime<Utc>>,
    pub timestamp_sensor_receive: Option<DateTime<Utc>>,
    pub started_at: DateTime<Utc>,
    pub completed_at: DateTime<Utc>,
    pub server_version: Option<u8>,
    pub client_version: Option<u8>,
    pub server_mode: Option<u8>,
    pub client_mode: Option<u8>,
    pub stratum: Option<u8>,
    pub leap_indicator: Option<u8>,
    pub precision: Option<i8>,
    pub poll_interval: Option<i8>,
    pub root_delay_seconds: Option<f64>,
    pub root_dispersion_seconds:Option<f64>,
    pub reference_id: Option<u32>,
    pub delay_seconds: Option<f64>,
    pub offset_seconds: Option<f64>,
    pub rtt_seconds: Option<f64>
}

pub fn generate(txs: &MutexGuard<HashMap<u32, NtpTransaction>>) -> NtpTransactionsReport {
    let mut transactions: Vec<NtpTransactionReport> = Vec::new();

    for tx in txs.values() {

    }

    NtpTransactionsReport { transactions }
}