use std::sync::{Arc, MutexGuard};
use chrono::{DateTime, Utc};
use serde::Serialize;
use crate::wired::packets::ArpPacket;

#[derive(Serialize)]
pub struct ArpPacketsReport {
    packets: Vec<ArpPacketReport>
}

#[derive(Serialize)]
pub struct ArpPacketReport {
    pub ethernet_source_mac: String,
    pub ethernet_destination_mac: String,
    pub hardware_type: String,
    pub protocol_type: String,
    pub operation: String,
    pub arp_sender_mac: String,
    pub arp_sender_address: String,
    pub arp_target_mac: String,
    pub arp_target_address: String,
    pub size: u32,
    pub timestamp: DateTime<Utc>
}

pub fn generate(p: &Vec<Arc<ArpPacket>>) -> ArpPacketsReport {
    let mut packets: Vec<ArpPacketReport> = Vec::new();

    for packet in p.iter() {
        packets.push(ArpPacketReport {
            ethernet_source_mac: packet.ethernet_source_mac.clone(),
            ethernet_destination_mac: packet.ethernet_destination_mac.clone(),
            hardware_type: packet.hardware_type.to_string(),
            protocol_type: packet.protocol_type.to_string(),
            operation: packet.operation.to_string(),
            arp_sender_mac: packet.arp_sender_mac.clone(),
            arp_sender_address: packet.arp_sender_address.to_string(),
            arp_target_mac: packet.arp_target_mac.clone(),
            arp_target_address: packet.arp_target_address.to_string(),
            size: packet.size,
            timestamp: packet.timestamp
        })
    }

    ArpPacketsReport { packets }
}
