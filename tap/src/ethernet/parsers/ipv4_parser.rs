use std::sync::Arc;

use crate::{ethernet::packets::{EthernetPacket, IPv4Packet}};
use anyhow::{Result, bail};
use crate::helpers::network::to_ipv4_address;

pub fn parse(packet: &Arc<EthernetPacket>) -> Result<IPv4Packet> {
    if packet.data.is_empty() {
        bail!("Empty payload.")
    }

    let header_length = (&packet.data[0] & 0x0F)*32/8;

    if packet.data.len() < header_length as usize {
        bail!("Payload shorter than header length.");
    }
   
    let ttl = packet.data[8];
    let protocol = packet.data[9];
    let source_address = to_ipv4_address(&packet.data[12..16]);
    let destination_address = to_ipv4_address(&packet.data[16..20]);
    let payload = packet.data[20..packet.data.len()].to_vec();
    let size = packet.size;

    let p = IPv4Packet {
        source_mac: packet.source_mac.clone(),
        destination_mac: packet.destination_mac.clone(),
        header_length,
        source_address,
        destination_address,
        ttl,
        protocol,
        payload,
        size,
        timestamp: packet.timestamp
    };

    Ok(p) 
}
