use std::collections::{HashMap, HashSet};
use std::sync::MutexGuard;
use chrono::{DateTime, Utc};
use serde::Serialize;
use crate::wired::packets::{Dhcpv4Transaction, SocksTunnel};

#[derive(Serialize)]
pub struct DhcpTransactionsReport {
    four: Vec<Dhcpv4TransactionReport>
}

#[derive(Serialize)]
pub struct Dhcpv4TransactionReport {
    pub transaction_type: String,
    pub transaction_id: u32,
    pub client_mac: String,
    pub additional_client_macs: HashSet<String>,
    pub server_mac: Option<String>,
    pub additional_server_macs: HashSet<String>,
    pub offered_ip_addresses: HashSet<String>,
    pub requested_ip_address: Option<String>,
    pub options_fingerprint: Option<String>,
    pub additional_options_fingerprints: HashSet<String>,
    pub timestamps: HashMap<String, Vec<DateTime<Utc>>>,
    pub latest_packet: DateTime<Utc>,
    pub notes: HashSet<String>,
    pub complete: bool
}

pub fn generate(txs: &MutexGuard<HashMap<u32, Dhcpv4Transaction>>) -> DhcpTransactionsReport {
    let mut four: Vec<Dhcpv4TransactionReport> = Vec::new();

    for tx in txs.values() {
        four.push(Dhcpv4TransactionReport {
            transaction_type: tx.transaction_type.to_string(),
            transaction_id: tx.transaction_id,
            client_mac: tx.client_mac.to_string(),
            additional_client_macs: tx.additional_client_macs.clone(),
            server_mac: tx.server_mac.clone(),
            additional_server_macs: tx.additional_server_macs.clone(),
            offered_ip_addresses: tx.offered_ip_addresses.iter().map(|ip| ip.to_string()).collect(),
            requested_ip_address: tx.requested_ip_address.map(|ip| ip.to_string()),
            options_fingerprint: tx.options_fingerprint.clone(),
            additional_options_fingerprints: tx.additional_options_fingerprints.clone(),
            timestamps: tx.timestamps.iter().map(|(k, v)| (k.to_string(), v.clone())).collect(),
            latest_packet: tx.latest_packet,
            notes: tx.notes.iter().map(|note| note.to_string()).collect(),
            complete: tx.complete,
        })
    }

    DhcpTransactionsReport { four }
}