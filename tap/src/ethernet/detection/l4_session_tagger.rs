use std::collections::HashMap;
use std::sync::MutexGuard;
use log::info;
use crate::data::tcp_table::TcpSession;
use crate::ethernet::tcp_session_key::TcpSessionKey;

enum L4SessionTag {
    HttpUnencrypted,
    HttpTls,
    SmtpUnencrypted,
    ImapUnencrypted,
    Pop3Unencrypted,
    Ftp,
    Telnet
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

        if session.source_port == 11000 || session.destination_port == 11000 {
            info!("CLIENT TO SERVER: {:?}", String::from_utf8(client_to_server_data));
            info!("SERVER TO CLIENT: {:?}", String::from_utf8(server_to_client_data));
        }
    }
}

/*
 * Re-Tag? Check what tags are already attached? only allow one?
 */
fn tag(client_to_server: Vec<u8>, server_to_client: Vec<u8>) {
    // Remove all control characters. Case insensitivity. Ignore redirects.
    let cts = String::from_utf8(client_to_server);
    let stc = String::from_utf8(server_to_client);

    if ()
}