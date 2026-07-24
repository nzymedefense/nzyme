use std::sync::{Arc, Mutex};
use log::error;
use crate::wired::packets::SshSession;
use crate::state::tables::ssh_table::SshTable;

pub struct SshProcessor {
    table: Arc<Mutex<SshTable>>
}

impl SshProcessor {

    pub fn new(table: Arc<Mutex<SshTable>>) -> Self {
        Self { table }
    }

    pub fn process(&mut self, session: Arc<SshSession>) {
        match self.table.lock() {
            Ok(table) => table.register_session(session),
            Err(e) => error!("Could not acquire SSH session table mutex: {}", e)
        }
    }

}

