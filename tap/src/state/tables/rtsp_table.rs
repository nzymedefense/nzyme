use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use log::{error, info};
use crate::link::leaderlink::Leaderlink;
use crate::metrics::Metrics;
use crate::protocols::parsers::l4_key::L4Key;
use crate::state::tables::table_helpers::clear_mutex_hashmap;
use crate::wired::packets::RtspSession;

pub struct RtspTable {
    leaderlink: Arc<Mutex<Leaderlink>>,
    metrics: Arc<Mutex<Metrics>>,
    sessions: Mutex<HashMap<L4Key, RtspSession>>
}

impl RtspTable {

    pub fn new(leaderlink: Arc<Mutex<Leaderlink>>, metrics: Arc<Mutex<Metrics>>) -> Self {
        Self {
            leaderlink,
            metrics,
            sessions: Mutex::new(HashMap::new())
        }
    }

    pub fn register_session(&self, session_ref: Arc<RtspSession>) {
        let session = (*session_ref).clone(); // Escape Arc.

        match self.sessions.lock() {
            Ok(mut sessions) => {
                /*
                 * We insert new session or overwrite existing one. The tagger always returns a
                 * fully up-to-date representation of the session with all members accurate.
                 */
                sessions.insert(session.setup_tcp_session_key.clone(), session);
            },
            Err(e) => error!("Could not acquired RTSP sessions table mutex: {}", e)
        }
    }

    pub fn process_report(&self) {
        match self.sessions.lock() {
            Ok(sessions) => {
               // TODO implement
            },
            Err(e) => {
                error!("Could not acquire RTSP sessions table mutex for report generation: {}", e);
            }
        }

        // Clean up.
        clear_mutex_hashmap(&self.sessions);
    }

    pub fn calculate_metrics(&self) {
        let sessions_size: i128 = match self.sessions.lock() {
            Ok(s) => s.len() as i128,
            Err(e) => {
                error!("Could not acquire mutex to calculate RTSP sessions table size: {}", e);

                -1
            }
        };

        match self.metrics.lock() {
            Ok(mut metrics) => {
                metrics.set_gauge("tables.rtsp.sessions.size", sessions_size);
            },
            Err(e) => error!("Could not acquire metrics mutex: {}", e)
        }
    }

}