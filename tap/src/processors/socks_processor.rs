use std::sync::{Arc, Mutex};
use log::error;
use crate::data::socks_table::SocksTable;
use crate::ethernet::packets::SocksTunnel;
use crate::metrics::Metrics;

pub struct SocksProcessor {
    table: Arc<Mutex<SocksTable>>,
    metrics: Arc<Mutex<Metrics>>
}

impl SocksProcessor {

    pub fn new(metrics: Arc<Mutex<Metrics>>, table: Arc<Mutex<SocksTable>>) -> Self {
        Self { metrics, table }
    }

    pub fn process(&mut self, tunnel: Arc<SocksTunnel>) {
        match self.table.lock() {
            Ok(mut table) => table.register_tunnel(tunnel),
            Err(e) => error!("Could not acquire SOCKS table mutex: {}", e)
        }
    }

}

