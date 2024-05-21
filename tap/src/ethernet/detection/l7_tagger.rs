use std::collections::HashMap;
use std::sync::{MutexGuard};
use log::info;
use strum_macros::Display;
use crate::data::tcp_table::TcpSession;
use crate::ethernet::detection::l7_tagger::L7SessionTag::{Http, Socks, Unencrypted};
use crate::ethernet::detection::taggers::{http_tagger, socks_tagger};
use crate::ethernet::tcp_session_key::TcpSessionKey;

#[derive(Debug, Display, PartialEq, Clone)]
pub enum L7SessionTag {
    Unencrypted,
    Http,
    Smtp,
    Imap,
    Pop3,
    Ftp,
    Telnet,
    Tls,
    Dns,
    Socks
}

pub fn tag_tcp_sessions(sessions: &mut MutexGuard<HashMap<TcpSessionKey, TcpSession>>) {
    for session in sessions.values_mut() {
        let mut client_to_server_data: Vec<u8> = vec![];
        let mut server_to_client_data: Vec<u8> = vec![];

        for segment_data in session.segments_client_to_server.values() {
            client_to_server_data.extend(segment_data);
        }

        for segment_data in session.segments_server_to_client.values() {
            server_to_client_data.extend(segment_data);
        }

        // We overwrite the tags because they may have changed with more segments coming in.
        session.tags = tag_all(client_to_server_data, server_to_client_data, session);
    }
}

fn tag_all(client_to_server: Vec<u8>, server_to_client: Vec<u8>, session: &TcpSession) -> Vec<L7SessionTag> {
    // Remove all control characters. Case insensitivity. Ignore redirects.
    let cts = String::from_utf8_lossy(&client_to_server).to_string();
    let stc = String::from_utf8_lossy(&server_to_client).to_string();

    let mut tags = Vec::new();

    if http_tagger::tag(&cts, &stc) {
        tags.extend([Unencrypted, Http]);
    }

    if socks_tagger::tag(&client_to_server, &server_to_client) {
        tags.extend([Socks]);
    }

    if tags.contains(&Socks) {
        info!("Detected new SOCKS4 ({:?}) TCP session: {} {}:{} -> {}:{} ({} byte)", tags, session.state, session.source_address, session.source_port, session.destination_address, session.destination_port, session.bytes_count);
    }

    tags
}