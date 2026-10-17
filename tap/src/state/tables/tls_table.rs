use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use log::error;
use crate::protocols::parsers::l4_key::L4Key;
use crate::link::leaderlink::Leaderlink;
use crate::metrics::Metrics;
use crate::state::tables::table_helpers::clear_mutex_hashmap;
use crate::wired::ethernet::types::tls_session::TlsSession;

pub struct TlsTable {
    #[allow(dead_code)]
    leaderlink: Arc<Mutex<Leaderlink>>,
    metrics: Arc<Mutex<Metrics>>,
    sessions: Mutex<HashMap<L4Key, TlsSession>>
}

impl TlsTable {

    pub fn new(leaderlink: Arc<Mutex<Leaderlink>>, metrics: Arc<Mutex<Metrics>>) -> Self {
        Self {
            leaderlink,
            metrics,
            sessions: Mutex::new(HashMap::new())
        }
    }

    pub fn register_session(&self, session_ref: Arc<TlsSession>) {
        let session = (*session_ref).clone();

        match self.sessions.lock() {
            Ok(mut sessions) => {
                sessions.insert(session.tcp_session_key.clone(), session);
            },
            Err(e) => error!("Could not acquire TLS sessions table mutex: {}", e)
        }
    }
    
    pub fn process_report(&self) {
        clear_mutex_hashmap(&self.sessions);
    }

    pub fn calculate_metrics(&self) {
        let sessions_size: i128 = match self.sessions.lock() {
            Ok(s) => s.len() as i128,
            Err(e) => {
                error!("Could not acquire mutex to calculate TLS sessions table size: {}", e);

                -1
            }
        };

        match self.metrics.lock() {
            Ok(mut metrics) => {
                metrics.set_gauge("tables.tls.sessions.size", sessions_size);
            },
            Err(e) => error!("Could not acquire metrics mutex: {}", e)
        }
    }

}
