
use std::{sync::{Arc}};
use std::collections::hash_map::ValuesMut;
use std::collections::HashMap;
use std::net::IpAddr;
use std::sync::{Mutex, MutexGuard};
use chrono::{DateTime, NaiveDateTime, Utc};
use log::{error, info, trace, warn};
use polars::df;
use polars::prelude::*;
use strum_macros::Display;
use crate::data::tcp_table::TcpSessionState::{ClosedFin, ClosedRst, ClosedTimeout, Established, FinWait1, FinWait2, Refused, SynReceived, SynSent};
use crate::ethernet::packets::{TcpSegment};
use crate::ethernet::tcp_session_key::TcpSessionKey;

static SESSION_TIMEOUT: usize = 60;

pub struct TcpTable {
    pub sessions: Mutex<HashMap<TcpSessionKey, TcpSession>>,
}

#[derive(Debug)]
pub struct TcpSession {
    pub state: TcpSessionState,
    pub source_address: IpAddr,
    pub source_port: u16,
    pub destination_address: IpAddr,
    pub destination_port: u16,
    pub start_time: DateTime<Utc>,
    pub end_time: Option<DateTime<Utc>>,
    pub segment_count: u64,
    pub bytes_count: u64,
    pub content: Vec<u8>
}

#[derive(PartialEq, Debug, Display, Clone)]
pub enum TcpSessionState {
    SynSent,
    SynReceived,
    Established,
    FinWait1,
    FinWait2,
    ClosedFin,
    ClosedRst,
    ClosedTimeout,
    Refused
}

impl TcpTable {

    pub fn new() -> Self {
        Self {
            sessions: Mutex::new(HashMap::new())
        }
    }

    pub fn register_segment(&mut self, segment: &Arc<TcpSegment>) {
        match self.sessions.lock() {
            Ok(mut sessions) => {
                match sessions.get_mut(&segment.session_key) {
                    Some(session) => {
                        let session_state = Self::determine_session_state(segment, Some(session));

                        session.state = session_state.clone();
                        session.segment_count += 1;
                        session.bytes_count += segment.size as u64;

                        if session.end_time.is_none() && (session_state == ClosedFin || session_state == ClosedRst) {
                            session.end_time = Some(Utc::now());
                        }

                        trace!("Existing TCP Session: {:?}, State: {:?}, Flags: {:?}",
                            segment.session_key, session_state, segment.flags);
                    },
                    None => {
                        let session_state = Self::determine_session_state(segment, None);

                        // We only record new connections, not mid-connection.
                        if session_state == SynSent {
                            let new_session = TcpSession {
                                state: session_state.clone(),
                                start_time: segment.timestamp,
                                end_time: None,
                                source_address: segment.source_address,
                                source_port: segment.source_port,
                                destination_address: segment.destination_address,
                                destination_port: segment.destination_port,
                                segment_count: 1,
                                bytes_count: segment.size as u64,
                                content: Vec::new() // TODO
                            };

                            trace!("New TCP Session: {:?}, State: {:?}, Flags: {:?}",
                            segment.session_key, session_state, segment.flags);

                            sessions.insert(segment.session_key.clone(), new_session);
                        }
                    }
                }
            },
            Err(e) => {
                error!("Could not acquire TCP sessions table mutex: {}", e);
            }
        }
    }

    pub fn process_report(&self) { // -> Report
        match self.sessions.lock() {
            Ok(mut sessions) => {
                // 0) Mark all timed out sessions in table as ClosedTimeout.
                timeout_sweep(&mut sessions);

                // 1) Write current table data to dataframe.
                let mut session_keys: Vec<u64> = Vec::new();
                let mut states: Vec<String> = Vec::new();
                let mut start_times: Vec<NaiveDateTime> = Vec::new();
                let mut end_times: Vec<Option<NaiveDateTime>> = Vec::new();
                let mut source_addresses: Vec<String> = Vec::new();
                let mut source_ports: Vec<u16> = Vec::new();
                let mut destination_addresses: Vec<String> = Vec::new();
                let mut destination_ports: Vec<u16> = Vec::new();
                let mut segment_counts: Vec<u64> = Vec::new();
                let mut byte_counts: Vec<u64> = Vec::new();

                for (session_key, session) in &*sessions {
                    session_keys.push(session_key.calculate_hash());
                    states.push(session.state.to_string());
                    start_times.push(session.start_time.naive_utc());
                    end_times.push(session.end_time.map(|set| set.naive_utc()));
                    source_addresses.push(session.source_address.to_string());
                    source_ports.push(session.source_port);
                    destination_addresses.push(session.destination_address.to_string());
                    destination_ports.push(session.destination_port);
                    segment_counts.push(session.segment_count);
                    byte_counts.push(session.bytes_count);
                }

                let df = df!(
                    "session" => &session_keys,
                    "state" => &states,
                    "start_time" => &start_times,
                    "end_time" => &end_times,
                    "source_address" => &source_addresses,
                    "source_port" => &source_ports,
                    "destination_address" => &destination_addresses,
                    "destination_port" => &destination_ports,
                    "segment_count" => &segment_counts,
                    "byte_count" => &byte_counts
                ).unwrap().lazy();

                // 2) Query Data and build report.
                info!("DATA: {}", df.collect().unwrap());

                // 3) Send data. Only proceed with cleanup if successful.

                // 4) Delete all timed out and closedfin/closedrst sessions in table.
            },
            Err(e) => {
                error!("Could not acquire TCP sessions table mutex: {}", e);
            }
        }
    }

    fn determine_session_state(segment: &TcpSegment, session: Option<&TcpSession>)
        -> TcpSessionState {
        match session {
            Some(session) => {
                // Do not re-open closed connections in case of out-of-order segments.
                if session.state == ClosedFin { return ClosedFin }
                if session.state == ClosedRst { return ClosedRst }
                if session.state == ClosedTimeout { return ClosedTimeout }

                if segment.flags.syn && segment.flags.ack {
                    // SYN ACK.
                    SynReceived
                } else if segment.flags.ack && session.state == SynReceived {
                    // Final ACK. Handshake complete.
                    Established
                } else if segment.flags.reset && session.state == SynSent {
                    // SYN got an immediate RST response. Connection rejected by host.
                    Refused
                } else if segment.flags.reset {
                    ClosedRst
                } else if segment.flags.fin && session.state == Established {
                    // Initial FIN
                    FinWait1
                } else if segment.flags.ack && session.state == FinWait1 {
                    // FIN ACK
                    FinWait2
                } else if segment.flags.ack && session.state == FinWait2 {
                    // Final FIN.
                    ClosedFin
                } else {
                    Established
                }
            },
            None => {
                // First time we see this session.
                if segment.flags.syn && !segment.flags.ack {
                    SynSent
                } else if segment.flags.syn && segment.flags.ack {
                    SynReceived
                } else if !segment.flags.syn && segment.flags.ack {
                    /*
                     * Either normal segment flow in established connection or final
                     * ACK in handshake or final ACK in FIN process. Considering that there was
                     * no previous segment recorded, we assume the likeliest case: A segment
                     * exchanged during an established connection.
                     *
                     * We'd falsely mark this connection as ESTABLISHED and have it time out if
                     * this was a FIN ACK and accept that risk, as it can only happen right after
                     * capture start.
                     *
                     * Additionally, the processing logic is only recording newly established
                     * connections and discarding anything else.
                     */
                    Established
                } else if segment.flags.fin {
                    FinWait1
                } else if segment.flags.reset {
                    ClosedRst
                } else {
                    warn!("Unexpected flags for new TCP session: <{:?}> {:?}",
                        segment.session_key, segment.flags);

                    Established
                }
            }
        }
    }



}

fn timeout_sweep(sessions: &mut MutexGuard<HashMap<TcpSessionKey, TcpSession>>) {
    for session in sessions.values_mut() {
        if session.last_segment
    }
}