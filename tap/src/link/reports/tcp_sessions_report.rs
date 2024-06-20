use std::collections::{HashMap};
use std::sync::MutexGuard;
use chrono::{DateTime, Utc};
use serde::Serialize;
use crate::ethernet::tables::tcp_table::TcpSession;
use crate::ethernet::tcp_session_key::TcpSessionKey;

#[derive(Serialize)]
pub struct TcpSessionsReport {
    pub sessions: Vec<TcpSessionReport>
}

#[derive(Serialize)]
pub struct TcpSessionReport {
    pub state: String,
    pub source_mac: String,
    pub destination_mac: String,
    pub source_address: String,
    pub source_port: u16,
    pub destination_address: String,
    pub destination_port: u16,
    pub start_time: DateTime<Utc>,
    pub end_time: Option<DateTime<Utc>>,
    pub most_recent_segment_time: DateTime<Utc>,
    pub segments_count: u64,
    pub bytes_count: u64,
    pub tags: Vec<String>
}

pub fn generate(s: &MutexGuard<HashMap<TcpSessionKey, TcpSession>>) -> TcpSessionsReport {
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
            tags: session.tags.clone().into_iter()
                        .map(|t| t.to_string())
                        .collect()
        })
    }

    TcpSessionsReport {
        sessions
    }
}