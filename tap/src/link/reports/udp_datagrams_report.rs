use std::sync::{Arc, MutexGuard};
use chrono::{DateTime, Utc};
use log::error;
use serde::Serialize;
use crate::wired::packets::Datagram;

#[derive(Serialize)]
pub struct UdpDatagramsReport {
    pub datagrams: Vec<UdpDatagramReport>
}

#[derive(Serialize)]
pub struct UdpDatagramReport {
    pub source_mac: Option<String>,
    pub destination_mac: Option<String>,
    pub source_address: String,
    pub destination_address: String,
    pub source_port: u16,
    pub destination_port: u16,
    pub bytes_count: u32,
    pub timestamp: DateTime<Utc>,
    pub tags: Vec<String>
}

pub fn generate(ds: &MutexGuard<Vec<Arc<Datagram>>>) -> UdpDatagramsReport {
    let mut datagrams: Vec<UdpDatagramReport> = Vec::new();

    for datagram in ds.iter() {
        let tags = match datagram.tags.lock() {
            Ok(tags) => tags.iter().map(|t| t.to_string()).collect(),
            Err(e) => {
                error!("Could not acquire mutex of datagram tags: {}", e);
                Vec::new()
            }
        };

        datagrams.push(UdpDatagramReport {
            source_mac: datagram.source_mac.clone(),
            destination_mac: datagram.destination_mac.clone(),
            source_address: datagram.source_address.to_string(),
            destination_address: datagram.destination_address.to_string(),
            source_port: datagram.source_port,
            destination_port: datagram.destination_port,
            bytes_count: datagram.size,
            timestamp: Default::default(),
            tags
        })
    }

    UdpDatagramsReport { datagrams }
}