use std::collections::{HashMap};
use std::sync::MutexGuard;
use chrono::{DateTime, Utc};
use serde::Serialize;
use crate::state::tables::tcp_table::TcpSession;
use crate::protocols::parsers::l4_key::L4Key;

#[derive(Serialize)]
pub struct TcpSessionsReport {
    pub sessions: Vec<TcpSessionReport>
}

#[derive(Serialize)]
pub struct TcpSessionReport {
    pub state: String,
    pub source_mac: Option<String>,
    pub destination_mac: Option<String>,
    pub source_address: String,
    pub source_port: u16,
    pub destination_address: String,
    pub destination_port: u16,
    pub start_time: DateTime<Utc>,
    pub end_time: Option<DateTime<Utc>>,
    pub most_recent_segment_time: DateTime<Utc>,
    pub segments_count: u64,
    pub bytes_count: u64,
    pub syn_ip_ttl: u8,
    pub syn_ip_tos: u8,
    pub syn_ip_df: bool,
    pub syn_cwr: bool,
    pub syn_ece: bool,
    pub syn_window_size: u16,
    pub syn_maximum_segment_size: Option<u16>,
    pub syn_window_scale_multiplier: Option<u8>,
    pub syn_options: Vec<u8>,
    pub tags: Vec<String>
}

pub fn generate(s: &MutexGuard<HashMap<L4Key, TcpSession>>) -> TcpSessionsReport {
    let mut sessions: Vec<TcpSessionReport> = Vec::new();

    for session in s.values() {
        sessions.push(TcpSessionReport {
            state: session.state.to_string(),
            source_mac: session.source_mac.clone(),
            destination_mac: session.destination_mac.clone(),
            source_address: session.source_address.to_string(),
            source_port: session.source_port,
            destination_address: session.destination_address.to_string(),
            destination_port: session.destination_port,
            start_time: session.start_time,
            end_time: session.end_time,
            most_recent_segment_time: session.most_recent_segment_time,
            segments_count: session.segments_count,
            bytes_count: session.bytes_count,
            syn_ip_ttl: session.syn_ip_ttl,
            syn_ip_tos: session.syn_ip_tos,
            syn_ip_df: session.syn_ip_df,
            syn_cwr: session.syn_cwr,
            syn_ece: session.syn_ece,
            syn_window_size: session.syn_window_size,
            syn_maximum_segment_size: session.syn_maximum_segment_size,
            syn_window_scale_multiplier: session.syn_window_scale_multiplier,
            syn_options: session.syn_options.clone(),
            tags: session.tags.clone().into_iter()
                        .map(|t| t.to_string())
                        .collect()
        })
    }

    TcpSessionsReport {
        sessions
    }
}