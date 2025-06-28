use std::collections::HashMap;
use std::sync::MutexGuard;
use chrono::{DateTime, Utc};
use serde::Serialize;
use crate::wired::packets::SocksTunnel;
use crate::protocols::parsers::l4_key::L4Key;

#[derive(Serialize)]
pub struct SocksTunnelsReport {
    pub tunnels: Vec<SocksTunnelReport>
}

#[derive(Serialize)]
pub struct SocksTunnelReport {
    pub socks_type: String,
    pub authentication_status: String,
    pub handshake_status: String,
    pub connection_status: String,
    pub username: Option<String>,
    pub tunneled_bytes: u64,
    pub tunneled_destination_address: Option<String>,
    pub tunneled_destination_host: Option<String>,
    pub tunneled_destination_port: u16,
    pub source_mac: Option<String>,
    pub destination_mac: Option<String>,
    pub source_address: String,
    pub source_port: u16,
    pub destination_address: String,
    pub destination_port: u16,
    pub established_at: DateTime<Utc>,
    pub terminated_at: Option<DateTime<Utc>>,
    pub most_recent_segment_time: DateTime<Utc>
}

pub fn generate(t: &MutexGuard<HashMap<L4Key, SocksTunnel>>) -> SocksTunnelsReport {
    let mut tunnels: Vec<SocksTunnelReport> = Vec::new();

    for tunnel in t.values() {
        tunnels.push(SocksTunnelReport {
            socks_type: tunnel.socks_type.to_string(),
            authentication_status: tunnel.authentication_status.to_string(),
            handshake_status: tunnel.handshake_status.to_string(),
            connection_status: tunnel.connection_status.to_string(),
            username: tunnel.username.clone(),
            tunneled_bytes: tunnel.tunneled_bytes,
            tunneled_destination_address: tunnel.tunneled_destination_address.map(|x| x.to_string()),
            tunneled_destination_host: tunnel.tunneled_destination_host.clone().map(|x| x.to_string()),
            tunneled_destination_port: tunnel.tunneled_destination_port,
            source_mac: tunnel.source_mac.clone(),
            destination_mac: tunnel.destination_mac.clone(),
            source_address: tunnel.source_address.to_string(),
            source_port: tunnel.source_port,
            destination_address: tunnel.destination_address.to_string(),
            destination_port: tunnel.destination_port,
            established_at: tunnel.established_at,
            terminated_at: tunnel.terminated_at,
            most_recent_segment_time: tunnel.most_recent_segment_time
        })
    }

    SocksTunnelsReport { tunnels }
}
