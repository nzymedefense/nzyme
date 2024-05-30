use std::sync::{Arc, Mutex};
use log::error;
use crate::data::ssh_table::SshTable;
use crate::ethernet::packets::SshSession;
use crate::metrics::Metrics;

pub struct SshProcessor {
    table: Arc<Mutex<SshTable>>,
    metrics: Arc<Mutex<Metrics>>
}

impl SshProcessor {

    pub fn new(metrics: Arc<Mutex<Metrics>>, table: Arc<Mutex<SshTable>>) -> Self {
        Self { metrics, table }
    }

    pub fn process(&mut self, session: Arc<SshSession>) {
        match self.table.lock() {
            Ok(mut table) => table.register_session(session),
            Err(e) => error!("Could not acquire SSH session table mutex: {}", e)
        }
    }

}

