use std::collections::{HashMap, HashSet};
use std::sync::MutexGuard;
use chrono::{DateTime, Utc};
use serde::{Serialize, Serializer};
use serde::ser::SerializeMap;
use crate::wired::packets::Dhcpv4Transaction;

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
    #[serde(serialize_with = "serialize_timestamps")] // We need microsecond resolution.
    pub timestamps: HashMap<String, Vec<DateTime<Utc>>>,
    pub first_packet: DateTime<Utc>,
    pub latest_packet: DateTime<Utc>,
    pub notes: HashSet<String>,
    pub successful: Option<bool>,
    pub complete: bool
}

fn serialize_timestamps<S>(
    timestamps: &HashMap<String, Vec<DateTime<Utc>>>,
    serializer: S,
) -> Result<S::Ok, S::Error>
where
    S: Serializer,
{
    let mut map = serializer.serialize_map(Some(timestamps.len()))?;
    for (key, vec) in timestamps {
        let strs: Vec<String> = vec
            .iter()
            .map(|dt| {
                let whole     = dt.format("%Y-%m-%dT%H:%M:%S");
                let frac4     = dt.timestamp_subsec_nanos() / 100_000;
                format!("{}.{:04}Z", whole, frac4)
            })
            .collect();
        map.serialize_entry(key, &strs)?;
    }
    map.end()
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
            first_packet: tx.first_packet,
            latest_packet: tx.latest_packet,
            notes: tx.notes.iter().map(|note| note.to_string()).collect(),
            successful: tx.successful,
            complete: tx.complete,
        })
    }

    DhcpTransactionsReport { four }
}