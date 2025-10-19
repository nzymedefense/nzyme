use std::collections::HashMap;
use std::net::IpAddr;
use std::sync::{Arc, Mutex, MutexGuard};
use chrono::{DateTime, Duration, Utc};
use log::{error, info};
use strum_macros::Display;
use crate::helpers::timer::{record_timer, Timer};
use crate::wired::packets::Datagram;
use crate::link::leaderlink::Leaderlink;
use crate::link::reports::udp_conversations_report;
use crate::metrics::Metrics;
use crate::protocols::detection::l7_tagger::L7Tag;
use crate::protocols::parsers::l4_key::L4Key;
use crate::state::tables::tcp_table::TcpSession;

pub struct UdpTable {
    leaderlink: Arc<Mutex<Leaderlink>>,
    metrics: Arc<Mutex<Metrics>>,
    conversations: Mutex<HashMap<L4Key, UdpConversation>>,
}

#[derive(Debug)]
pub struct UdpConversation {
    pub state: UdpConversationState,
    pub source_mac: Option<String>,
    pub destination_mac: Option<String>,
    pub source_address: IpAddr,
    pub source_port: u16,
    pub destination_address: IpAddr,
    pub destination_port: u16,
    pub start_time: DateTime<Utc>,
    pub end_time: Option<DateTime<Utc>>,
    pub most_recent_segment_time: DateTime<Utc>,
    pub datagrams_count: u64,
    pub datagrams_count_incremental: u64, // New datagrams since last report.
    pub bytes_count: u64,
    pub bytes_count_incremental: u64, // New bytes since last report.
    pub datagrams: Vec<Arc<Datagram>>,
    pub tags: Vec<L7Tag>
}

#[derive(PartialEq, Debug, Display, Clone)]
pub enum UdpConversationState {
    Active,
    Closed
}

impl UdpTable {

    pub fn new(leaderlink: Arc<Mutex<Leaderlink>>, metrics: Arc<Mutex<Metrics>>) -> Self {
        Self {
            leaderlink,
            metrics,
            conversations: Mutex::new(HashMap::new())
        }
    }

    pub fn register_datagram(&mut self, datagram: Arc<Datagram>) {
        match self.conversations.lock() {
            Ok(mut conversations) => {
                match conversations.get_mut(&datagram.session_key) {
                    Some(c) => {
                        // Existing conversation.
                        let mut timer = Timer::new();

                        c.most_recent_segment_time = datagram.timestamp;
                        c.datagrams_count += 1;
                        c.datagrams_count_incremental += 1;
                        c.bytes_count += datagram.payload.len() as u64;
                        c.bytes_count_incremental += datagram.payload.len() as u64;
                        c.datagrams.push(datagram);

                        timer.stop();
                        record_timer(
                            timer.elapsed_microseconds(),
                            "tables.udp.conversations.timer.register_existing",
                            &self.metrics
                        );
                    },
                    None => {
                        // New conversation.
                        let mut timer = Timer::new();

                        conversations.insert(
                            datagram.session_key.clone(),
                            UdpConversation {
                                state: UdpConversationState::Active,
                                source_mac: datagram.source_mac.clone(),
                                destination_mac: datagram.destination_mac.clone(),
                                source_address: datagram.source_address,
                                source_port: datagram.source_port,
                                destination_address: datagram.destination_address,
                                destination_port: datagram.destination_port,
                                start_time: datagram.timestamp,
                                end_time: None,
                                most_recent_segment_time: datagram.timestamp,
                                datagrams_count: 1,
                                datagrams_count_incremental: 1,
                                bytes_count: datagram.payload.len() as u64,
                                bytes_count_incremental: datagram.payload.len() as u64,
                                datagrams: vec![datagram],
                                tags: vec![],
                            }
                        );

                        timer.stop();
                        record_timer(
                            timer.elapsed_microseconds(),
                            "tables.udp.conversations.timer.register_new",
                            &self.metrics
                        );
                    }
                }
            }
            Err(e) => {
                error!("Could not acquire UDP conversations table mutex: {}", e);
            }
        }
    }

    pub fn process_report(&self) {
        match self.conversations.lock() {
            Ok(mut conversations) => {
                // Set end time and state in all expired conversations.
                for c in conversations.values_mut() {
                    if Utc::now() - c.most_recent_segment_time >
                        Duration::try_seconds(60).unwrap() {

                        c.end_time = Some(c.most_recent_segment_time);
                        c.state = UdpConversationState::Closed;
                    }
                }

                // Generate JSON.
                let mut timer = Timer::new();
                let report = match serde_json::to_string(&udp_conversations_report::generate(&conversations)) {
                    Ok(report) => report,
                    Err(e) => {
                        error!("Could not serialize UDP conversations report: {}", e);
                        return;
                    }
                };
                timer.stop();
                record_timer(
                    timer.elapsed_microseconds(),
                    "tables.udp.timer.report_generation",
                    &self.metrics
                );

                // Send report.
                match self.leaderlink.lock() {
                    Ok(link) => {
                        if let Err(e) = link.send_report("udp/conversations", report) {
                            error!("Could not submit UDP conversations report: {}", e);
                        }
                    },
                    Err(e) => error!("Could not acquire leader link lock for UDP conversations \
                        report submission: {}", e)
                }

                // Delete all closed conversations.
                conversations.retain(|_key, c| c.state != UdpConversationState::Closed);

                // Reset all incremental counters.
                incremental_counter_sweep(&mut conversations);
            },
            Err(e) => {
                error!("Could not acquire UDP conversations table mutex for report \
                    generation and maintenance: {}", e);
            }
        }
    }

    pub fn calculate_metrics(&self) {
        let conversations_size: i128 = match self.conversations.lock() {
            Ok(c) => c.len() as i128,
            Err(e) => {
                error!("Could not acquire mutex to calculate UDP conversation table size: {}", e);

                -1
            }
        };

        match self.metrics.lock() {
            Ok(mut metrics) => {
                metrics.set_gauge("tables.udp.conversations.size", conversations_size);
            },
            Err(e) => error!("Could not acquire metrics mutex: {}", e)
        }
    }

}

fn incremental_counter_sweep(sessions: &mut MutexGuard<HashMap<L4Key, UdpConversation>>) {
    for session in sessions.values_mut() {
        session.datagrams_count_incremental = 0;
        session.bytes_count_incremental = 0;
    }
}