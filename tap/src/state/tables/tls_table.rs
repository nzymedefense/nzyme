use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use log::error;
use crate::protocols::parsers::l4_key::L4Key;
use crate::helpers::timer::{record_timer, Timer};
use crate::link::leaderlink::Leaderlink;
use crate::link::reports::tls_sessions_report;
use crate::metrics::Metrics;
use crate::state::tables::table_helpers::clear_mutex_hashmap;
use crate::wired::ethernet::types::tls_session::TlsSession;

pub struct TlsTable {
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
        match self.sessions.lock() {
            Ok(sessions) => {
                // Generate JSON.
                let mut timer = Timer::new();
                let report = match serde_json::to_string(&tls_sessions_report::generate(&sessions)) {
                    Ok(report) => report,
                    Err(e) => {
                        error!("Could not serialize TLS sessions report: {}", e);
                        return;
                    }
                };
                timer.stop();
                record_timer(
                    timer.elapsed_microseconds(),
                    "tables.tls.timer.report_generation",
                    &self.metrics
                );

                // Send report.
                match self.leaderlink.lock() {
                    Ok(link) => {
                        if let Err(e) = link.send_report("tls/sessions", report) {
                            error!("Could not submit TLS sessions report: {}", e);
                        }
                    },
                    Err(e) => error!("Could not acquire leader link lock for TLS sessions \
                                        report submission: {}", e)
                }
            },
            Err(e) => {
                error!("Could not acquire TLS sessions table mutex for report generation: {}", e);
            }
        }

        // Clean up.
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
