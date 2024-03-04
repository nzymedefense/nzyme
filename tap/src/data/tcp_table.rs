
use std::{sync::{Arc}};
use std::collections::HashMap;
use std::sync::Mutex;
use chrono::{DateTime, Utc};
use log::{error, info, trace, warn};
use crate::data::tcp_table::TcpSessionState::{ClosedFin, ClosedRst, ClosedTimeout, Established, FinWait1, FinWait2, Refused, SynReceived, SynSent};
use crate::ethernet::packets::{TcpSegment};
use crate::ethernet::tcp_session_key::TcpSessionKey;

pub struct TcpTable {
    pub sessions: Mutex<HashMap<TcpSessionKey, TcpSession>>
}

#[derive(Debug)]
pub struct TcpSession {
    pub state: TcpSessionState,
    pub start_time: DateTime<Utc>,
    pub end_time: Option<DateTime<Utc>>,
    pub segment_count: u128,
    pub bytes_count: u128,
    pub content: Vec<u8>
}

#[derive(PartialEq, Debug, Clone)]
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

    // NEXT store, reassemble segment data
    pub fn register_segment(&mut self, segment: &Arc<TcpSegment>) {
        match self.sessions.lock() {
            Ok(mut sessions) => {
                match sessions.get_mut(&segment.session_key) {
                    Some(session) => {
                        let session_state = Self::determine_session_state(&segment, Some(session));

                        session.state = session_state.clone();
                        session.segment_count += 1;
                        session.bytes_count += segment.size as u128;

                        if session.end_time == None && (session_state == ClosedFin || session_state == ClosedRst) {
                            session.end_time = Some(Utc::now());
                        }

                        trace!("Existing TCP Session: {:?}, State: {:?}, Flags: {:?}",
                            segment.session_key, session_state, segment.flags);
                    },
                    None => {
                        let session_state = Self::determine_session_state(&segment, None);

                        let new_session = TcpSession {
                            state: session_state.clone(),
                            start_time: segment.timestamp,
                            end_time: None,
                            segment_count: 1,
                            bytes_count: segment.size as u128,
                            content: Vec::new() // TODO
                        };

                        trace!("New      TCP Session: {:?}, State: {:?}, Flags: {:?}",
                            segment.session_key, session_state, segment.flags);

                        sessions.insert(segment.session_key.clone(), new_session);
                    }
                }
            },
            Err(e) => {
                error!("Could not acquire TCP sessions table mutex: {}", e);
            }
        }
    }

    pub fn execute_background_jobs(&self) {
        info!("TABLE: {:?}", self.sessions);
    }

    /*
     * TODO
     *   - test this with all combinations
     *   - Can we improve initial state detection?
     */
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
                     * no previous segment recorded, we assume the likeliest case: A normal segment
                     * exchange during an established connection.
                     *
                     * We'd falsely mark this connection as ESTABLISHED and have it time out if
                     * this was a FIN ACK and accept that risk, as it can only happen right after
                     * capture start.
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