use std::collections::HashMap;
use std::sync::{Arc, MutexGuard};
use chrono::{DateTime, Utc};
use serde::Serialize;
use crate::protocols::parsers::l4_key::L4Key;
use crate::state::tables::udp_table::UdpConversation;

#[derive(Serialize)]
pub struct UdpConversationsReport {
    pub conversations: Vec<UdpConversationReport>
}

#[derive(Serialize)]
pub struct UdpConversationReport {
    pub state: String,
    pub source_mac: Option<String>,
    pub destination_mac: Option<String>,
    pub source_address: String,
    pub destination_address: String,
    pub source_port: u16,
    pub destination_port: u16,
    pub bytes_count: u64,
    pub datagrams_count: u64,
    pub start_time: DateTime<Utc>,
    pub end_time: Option<DateTime<Utc>>,
    pub most_recent_segment_time: DateTime<Utc>,
    pub tags: Vec<String>
}

pub fn generate(cvs: &MutexGuard<HashMap<L4Key, UdpConversation>>)
    -> UdpConversationsReport {
    let mut conversations: Vec<UdpConversationReport> = Vec::new();

    for c in cvs.values() {
        conversations.push(UdpConversationReport {
            state: c.state.to_string(),
            source_mac: c.source_mac.clone(),
            destination_mac: c.destination_mac.clone(),
            source_address: c.source_address.to_string(),
            destination_address: c.destination_address.to_string(),
            source_port: c.source_port,
            destination_port: c.destination_port,
            bytes_count: c.bytes_count,
            datagrams_count: c.datagrams_count,
            start_time: c.start_time,
            end_time: c.end_time,
            most_recent_segment_time: c.most_recent_segment_time,
            tags: vec![],
        })
    }

    UdpConversationsReport { conversations }
}