use std::collections::HashMap;
use std::sync::MutexGuard;
use chrono::{DateTime, Utc};
use serde::Serialize;
use crate::wired::packets::SshSession;
use crate::protocols::parsers::l4_key::L4Key;

#[derive(Serialize)]
pub struct SshSessionsReport {
    pub sessions: Vec<SshSessionReport>
}

#[derive(Serialize)]
pub struct SshSessionReport {
    pub client_version: SshVersionReport,
    pub server_version: SshVersionReport,
    pub connection_status: String,
    pub tunneled_bytes: u64,
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

#[derive(Serialize)]
pub struct SshVersionReport {
    pub version: String,
    pub software: String,
    pub comments: Option<String>
}

pub fn generate(s: &MutexGuard<HashMap<L4Key, SshSession>>) -> SshSessionsReport {
    let mut sessions: Vec<SshSessionReport> = Vec::new();

    for session in s.values() {
        sessions.push(SshSessionReport {
            client_version: SshVersionReport {
                version: session.client_version.version.clone(),
                software:  session.client_version.software.clone(),
                comments:  session.client_version.comments.clone()
            },
            server_version: SshVersionReport {
                version: session.server_version.version.clone(),
                software:  session.server_version.software.clone(),
                comments:  session.server_version.comments.clone()
            },
            connection_status: session.connection_status.to_string(),
            tunneled_bytes: session.tunneled_bytes,
            source_mac: session.source_mac.clone(),
            destination_mac: session.destination_mac.clone(),
            source_address: session.source_address.to_string(),
            source_port: session.source_port,
            destination_address: session.destination_address.to_string(),
            destination_port: session.destination_port,
            established_at: session.established_at,
            terminated_at: session.terminated_at,
            most_recent_segment_time: session.most_recent_segment_time
        })
    }

    SshSessionsReport { sessions }
}
