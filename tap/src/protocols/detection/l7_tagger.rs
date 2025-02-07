use std::collections::HashMap;
use std::panic;
use std::sync::{Arc, Mutex, MutexGuard};
use log::error;
use strum_macros::Display;
use crate::protocols::detection::l7_tagger::L7SessionTag::{Http, Socks, Ssh, Unencrypted};
use crate::state::tables::tcp_table::TcpSession;
use crate::protocols::parsers::tcp::tcp_session_key::TcpSessionKey;
use crate::helpers::timer::{record_timer, Timer};
use crate::messagebus::bus::Bus;
use crate::messagebus::channel_names::WiredChannelName;
use crate::metrics::Metrics;
use crate::protocols::detection::taggers::network_protocols::{http_tagger, socks_tagger, ssh_tagger};
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
    Socks,
    Dhcpv4
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

        /*
         * The taggers should always be written extremely defensively and never throw any panics.
         * However, to increase reliability, we catch and log any panics that may still occur
         * because nobody is perfect.
         */
        let result = panic::catch_unwind(|| tag_all(
            &client_to_server_data,
            &server_to_client_data,
            session,
            &bus,
            &metrics
        ));

        match result {
            Ok(tags) => {
                // We overwrite the tags because they may have changed with more segments coming in.
                session.tags = tags
            },
            Err(e) => {
                if let Some(s) = e.downcast_ref::<&str>() {
                    error!("Could not tag TCP session {:?}: {}", session.session_key, s);
                } else if let Some(s) = e.downcast_ref::<String>() {
                    error!("Could not tag TCP session {:?}: {}", session.session_key, s);
                } else {
                    error!("Could not tag TCP session {:?}. Panicked with an unknown type.",
                        session.session_key);
                }
            }
        }
    }
}

fn tag_all(client_to_server: &[u8],
           server_to_client: &[u8],
           session: &TcpSession,
           bus: &Arc<Bus>,
           metrics: &Arc<Mutex<Metrics>>) -> Vec<L7SessionTag> {
    let mut tags = Vec::new();

    // HTTP.
    let mut http_timer_untagged = Timer::new();
    let mut http_timer_tagged = Timer::new();
    if http_tagger::tag(client_to_server, server_to_client).is_some() {
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
    if let Some(socks) = socks_tagger::tag(client_to_server, server_to_client, session) {
        socks_timer_tagged.stop();
        record_timer(
            socks_timer_tagged.elapsed_microseconds(),
            "tables.tcp.timer.sessions.tagging.socks.tagged",
            metrics
        );

        let len = socks.estimate_struct_size();
        to_pipeline!(
            WiredChannelName::SocksPipeline,
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
    if let Some(ssh) = ssh_tagger::tag(client_to_server, server_to_client, session) {
        ssh_timer_tagged.stop();
        record_timer(
            ssh_timer_tagged.elapsed_microseconds(),
            "tables.tcp.timer.sessions.tagging.ssh.tagged",
            metrics
        );

        let len = ssh.estimate_struct_size();
        to_pipeline!(
            WiredChannelName::SshPipeline,
            bus.ssh_pipeline.sender,
            Arc::new(ssh),
            len
        );
        
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