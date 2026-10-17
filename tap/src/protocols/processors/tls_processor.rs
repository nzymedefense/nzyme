use std::sync::{Arc, Mutex};
use log::error;
use crate::state::tables::tls_table::TlsTable;
use crate::wired::ethernet::types::tls_session::TlsSession;

pub struct TlsProcessor {
    table: Arc<Mutex<TlsTable>>
}

impl TlsProcessor {

    pub fn new(table: Arc<Mutex<TlsTable>>) -> Self {
        Self { table }
    }

    pub fn process(&mut self, session: Arc<TlsSession>) {
        match self.table.lock() {
            Ok(table) => table.register_session(session),
            Err(e) => error!("Could not acquire TLS session table mutex: {}", e)
        }
    }

}
