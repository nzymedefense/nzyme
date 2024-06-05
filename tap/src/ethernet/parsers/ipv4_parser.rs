use std::sync::Arc;

use crate::{ethernet::packets::{EthernetPacket, IPv4Packet}};
use anyhow::{Result, bail};
use byteorder::{BigEndian, ByteOrder};
use crate::helpers::network::to_ipv4_address;

pub fn parse(packet: &Arc<EthernetPacket>) -> Result<IPv4Packet> {
    if packet.data.is_empty() {
        bail!("Empty payload.")
    }

    let header_length = match ((&packet.data[0] & 0x0F) as usize)
        .checked_mul(32)
        .and_then(|result| result.checked_div(8)) {
        Some(hl) => hl,
        None => { bail!("Header length calculation failed with too large numbers.") }
    };

    if packet.data.len() < header_length || packet.data.len() < 20  {
        bail!("Payload too short.");
    }

    let total_length = BigEndian::read_u16(&packet.data[2..4]) as usize;

    if total_length < header_length || total_length > packet.data.len() {
        bail!("Invalid total length.");
    }

    let ttl = packet.data[8];
    let protocol = packet.data[9];
    let source_address = to_ipv4_address(&packet.data[12..16]);
    let destination_address = to_ipv4_address(&packet.data[16..20]);
    let payload = packet.data[header_length..].to_vec();
    let size = packet.size;

    let p = IPv4Packet {
        source_mac: packet.source_mac.clone(),
        destination_mac: packet.destination_mac.clone(),
        header_length,
        total_length,
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
