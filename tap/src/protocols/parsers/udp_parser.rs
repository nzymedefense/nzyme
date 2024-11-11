use std::sync::Mutex;
use anyhow::{Result, bail};
use byteorder::{BigEndian, ByteOrder};

use crate::wired::packets::{Datagram, IPv4Packet};

#[allow(clippy::cast_possible_truncation)]
pub fn parse(ip4: IPv4Packet) -> Result<Datagram> {

    if ip4.payload.len() < 8 {
        bail!("Payload too short for UDP packet.");
    }

    let source_port = BigEndian::read_u16(&ip4.payload[0..2]);
    let destination_port = BigEndian::read_u16(&ip4.payload[2..4]);

    let payload = ip4.payload[8..ip4.payload.len()].to_vec();

    // This payload + this header (8) + IPv4 (20) + ethernet (14)
    let size = (payload.len() + 42) as u32;

    Ok(Datagram {
        source_mac: ip4.source_mac,
        destination_mac: ip4.destination_mac,
        source_address: ip4.source_address,
        destination_address: ip4.destination_address,
        source_port,
        destination_port,
        payload,
        size,
        timestamp: ip4.timestamp,
        tags: Mutex::new(vec![])
    })

}
