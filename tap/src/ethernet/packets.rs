use std::net::IpAddr;
use chrono::{DateTime, Utc};
use crate::ethernet::tcp_session_key::TcpSessionKey;

use super::types::{HardwareType, EtherType, ARPOpCode, DNSType, DNSClass, DNSDataType};

#[derive(Debug)]
pub struct EthernetData {
    pub data: Vec<u8>,
}

#[derive(Debug)]
pub struct EthernetPacket {
    pub source_mac: String,
    pub destination_mac: String,
    pub data: Vec<u8>,
    pub packet_type: EtherType,
    pub size: u32,
    pub timestamp: DateTime<Utc>
}

#[derive(Debug)]
pub struct ARPPacket {
    pub source_mac: String,
    pub destination_mac: String,
    pub hardware_type: HardwareType,
    pub protocol_type: EtherType,
    pub hardware_length: u8,
    pub protocol_length: u8,
    pub operation: ARPOpCode,
    pub sender_hardware_address: String,
    pub sender_protocol_address: String,
    pub target_hardware_address: String,
    pub target_protocol_address: String,
    pub size: u32,
    pub timestamp: DateTime<Utc>
}

#[derive(Debug)]
pub struct IPv4Packet {
    pub source_mac: String,
    pub destination_mac: String,
    pub header_length: u8,
    pub source_address: IpAddr,
    pub destination_address: IpAddr,
    pub ttl: u8,
    pub protocol: u8,
    pub payload: Vec<u8>,
    pub size: u32,
    pub timestamp: DateTime<Utc>
}

#[derive(Debug)]
pub struct TcpSegment {
    pub source_mac: String,
    pub destination_mac: String,
    pub source_address: IpAddr,
    pub destination_address: IpAddr,
    pub source_port: u16,
    pub destination_port: u16,
    pub session_key: TcpSessionKey,
    pub flags: TcpFlags,
    pub payload: Vec<u8>,
    pub size: u32,
    pub timestamp: DateTime<Utc>
}

#[derive(Debug)]
pub struct TcpFlags {
    pub ack: bool,
    pub reset: bool,
    pub syn: bool,
    pub fin: bool
}

#[derive(Debug)]
pub struct UDPPacket {
    pub source_mac: String,
    pub destination_mac: String,
    pub source_address: IpAddr,
    pub destination_address: IpAddr,
    pub source_port: u16,
    pub destination_port: u16,
    pub payload: Vec<u8>,
    pub size: u32,
    pub timestamp: DateTime<Utc>
}


#[derive(Debug)]
pub struct DNSPacket {
    pub source_mac: String,
    pub destination_mac: String,
    pub source_address: IpAddr,
    pub destination_address: IpAddr,
    pub source_port: u16,
    pub destination_port: u16,
    pub dns_type: DNSType,
    pub question_count: u16,
    pub answer_count: u16,
    pub queries: Option<Vec<DNSData>>,
    pub responses: Option<Vec<DNSData>>,
    pub size: u32,
    pub timestamp: DateTime<Utc>
}

#[derive(Debug)]
pub struct DNSData {
    pub name: String,
    pub dns_type: DNSDataType,
    pub class: DNSClass,
    pub value: Option<String>,
    pub ttl: Option<u32>,
    pub entropy: Option<f32>
}

#[derive(Debug)]
pub struct IPv6Packet { }
