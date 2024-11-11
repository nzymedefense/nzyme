use anyhow::{Result, bail};
use bitvec::order::Lsb0;
use bitvec::view::BitView;
use byteorder::{BigEndian, ByteOrder};
use chrono::Utc;
use crate::wired::packets::{IPv4Packet, TcpFlags, TcpSegment};
use crate::protocols::parsers::tcp::tcp_session_key::TcpSessionKey;

#[allow(clippy::cast_possible_truncation)]
pub fn parse(packet: IPv4Packet) -> Result<TcpSegment> {
    if packet.payload.len() < 15 {
        bail!("Payload too short for TCP packet.");
    }

    let source_port = BigEndian::read_u16(&packet.payload[0..2]);
    let destination_port = BigEndian::read_u16(&packet.payload[2..4]);

    let session_key = TcpSessionKey::new(
        packet.source_address, source_port, packet.destination_address, destination_port
    );

    let sequence_number = BigEndian::read_u32(&packet.payload[4..8]);
    let ack_number = BigEndian::read_u32(&packet.payload[8..12]);

    let header_length: usize = ((&packet.payload[12] >> 4)*4) as usize;

    let flags = &packet.payload[13..15].view_bits::<Lsb0>();
    let ack = *flags.get(4).unwrap();
    let reset = *flags.get(2).unwrap();
    let syn = *flags.get(1).unwrap();
    let fin = *flags.get(0).unwrap();

    let flags = TcpFlags { ack, reset, syn, fin };

    if packet.payload.len() < header_length {
        bail!("Payload shorter than header length.");
    }

    if packet.total_length < packet.header_length + header_length {
        bail!("Total length less than combined header lengths.");
    }

    let payload = packet.payload[header_length..packet.total_length-packet.header_length].to_vec();

    // This payload + this header + IPv4 (20) + ethernet (14)
    let size = (payload.len() + header_length + 34) as u32;

    Ok(TcpSegment {
        source_mac: packet.source_mac,
        destination_mac: packet.destination_mac,
        source_address: packet.source_address,
        destination_address: packet.destination_address,
        source_port,
        destination_port,
        session_key,
        sequence_number,
        ack_number,
        flags,
        payload,
        size, 
        timestamp: Utc::now() 
    })
}