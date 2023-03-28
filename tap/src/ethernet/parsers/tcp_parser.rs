use anyhow::{Result, bail};
use byteorder::{BigEndian, ByteOrder};
use chrono::Utc;
use crate::ethernet::packets::{IPv4Packet, TCPPacket};

#[allow(clippy::cast_possible_truncation)]
pub fn parse(ip4: IPv4Packet) -> Result<TCPPacket> {
    if ip4.payload.len() < 13 {
        bail!("Payload too short for TCP packet.");
    }

    let source_port = BigEndian::read_u16(&ip4.payload[0..2]);
    let destination_port = BigEndian::read_u16(&ip4.payload[2..4]);

    let header_length: usize = ((&ip4.payload[12] >> 4)*4) as usize;

    if ip4.payload.len() < header_length {
        bail!("Payload shorter than header length.");
    }

    let payload = ip4.payload[(header_length)..ip4.payload.len()].to_vec();

    // This payload + this header + IPv4 (20) + ethernet (14)
    let size = (payload.len() + header_length + 34) as u32;

    Ok(TCPPacket {
        source_mac: ip4.source_mac,
        destination_mac: ip4.destination_mac,
        source_address: ip4.source_address,
        destination_address: ip4.destination_address,
        source_port,
        destination_port,
        payload,
        size, 
        timestamp: Utc::now() 
    })
}