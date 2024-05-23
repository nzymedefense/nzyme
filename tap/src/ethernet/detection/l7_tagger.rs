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
    let client_to_server_string = String::from_utf8_lossy(&client_to_server).to_string();
    let server_to_client_string = String::from_utf8_lossy(&server_to_client).to_string();

    let mut tags = Vec::new();

    if http_tagger::tag(&client_to_server_string, &server_to_client_string).is_some() {
        tags.extend([Http, Unencrypted]);
    }

    // TODO match, send to socks channel/processor
    if let Some(socks) = socks_tagger::tag(&client_to_server, &server_to_client, session) {
        info!("SOCKS: {:?}", socks);
        
        tags.extend([Socks, Unencrypted]);
    }

    tags
}