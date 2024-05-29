use std::collections::HashMap;
use std::sync::{Arc, Mutex, MutexGuard};
use log::{error, info};
use strum_macros::Display;
use crate::data::tcp_table::TcpSession;
use crate::ethernet::detection::l7_tagger::L7SessionTag::{Http, Socks, Ssh, Unencrypted};
use crate::ethernet::detection::taggers::{http_tagger, socks_tagger, ssh_tagger};
use crate::ethernet::tcp_session_key::TcpSessionKey;
use crate::helpers::timer::{record_timer, Timer};
use crate::messagebus::bus::Bus;
use crate::messagebus::channel_names::EthernetChannelName;
use crate::metrics::Metrics;
use crate::to_pipeline;

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
    Ssh,
    Socks
}

pub fn tag_tcp_sessions(sessions: &mut MutexGuard<HashMap<TcpSessionKey, TcpSession>>,
                        bus: Arc<Bus>,
                        metrics: Arc<Mutex<Metrics>>) {
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
        session.tags = tag_all(
            client_to_server_data,
            server_to_client_data,
            session,
            &bus,
            &metrics
        );
    }
}

fn tag_all(client_to_server: Vec<u8>,
           server_to_client: Vec<u8>,
           session: &TcpSession,
           bus: &Arc<Bus>,
           metrics: &Arc<Mutex<Metrics>>) -> Vec<L7SessionTag> {
    let client_to_server_string = String::from_utf8_lossy(&client_to_server).to_string();
    let server_to_client_string = String::from_utf8_lossy(&server_to_client).to_string();

    let mut tags = Vec::new();

    // HTTP.
    let mut http_timer_untagged = Timer::new();
    let mut http_timer_tagged = Timer::new();
    if http_tagger::tag(&client_to_server_string, &server_to_client_string).is_some() {
        http_timer_tagged.stop();
        record_timer(
            http_timer_tagged.elapsed_microseconds(),
            "tables.tcp.timer.sessions.tagging.http.tagged",
            metrics
        );

        tags.extend([Http, Unencrypted]);
    } else {
        http_timer_untagged.stop();
        record_timer(
            http_timer_untagged.elapsed_microseconds(),
            "tables.tcp.timer.sessions.tagging.http.untagged",
            metrics
        );
    }

    // SOCKS.
    let mut socks_timer_untagged = Timer::new();
    let mut socks_timer_tagged = Timer::new();
    if let Some(socks) = socks_tagger::tag(&client_to_server, &server_to_client, session) {
        socks_timer_tagged.stop();
        record_timer(
            socks_timer_tagged.elapsed_microseconds(),
            "tables.tcp.timer.sessions.tagging.socks.tagged",
            metrics
        );

        let len = socks.estimate_struct_size();
        to_pipeline!(
            EthernetChannelName::SocksPipeline,
            bus.socks_pipeline.sender,
            Arc::new(socks),
            len
        );

        tags.extend([Socks, Unencrypted]);
    } else {
        socks_timer_untagged.stop();
        record_timer(
            socks_timer_untagged.elapsed_microseconds(),
            "tables.tcp.timer.sessions.tagging.socks.untagged",
            metrics
        );
    }
    
    // SSH.
    let mut ssh_timer_untagged = Timer::new();
    let mut ssh_timer_tagged = Timer::new();
    if let Some(ssh) = ssh_tagger::tag(&client_to_server, &server_to_client, session) {
        ssh_timer_tagged.stop();
        record_timer(
            ssh_timer_tagged.elapsed_microseconds(),
            "tables.tcp.timer.sessions.tagging.ssh.tagged",
            metrics
        );

        info!("SSH: {:?}", ssh);
        
        tags.extend([Ssh]);
    } else {
        ssh_timer_untagged.stop();
        record_timer(
            ssh_timer_untagged.elapsed_microseconds(),
            "tables.tcp.timer.sessions.tagging.ssh.untagged",
            metrics
        );
    }

    tags
}