use std::sync::{Arc, Mutex};
use log::error;
use crate::wired::packets::SocksTunnel;
use crate::state::tables::socks_table::SocksTable;

pub struct SocksProcessor {
    table: Arc<Mutex<SocksTable>>
}

impl SocksProcessor {

    pub fn new(table: Arc<Mutex<SocksTable>>) -> Self {
        Self { table }
    }

    pub fn process(&mut self, tunnel: Arc<SocksTunnel>) {
        match self.table.lock() {
            Ok(mut table) => table.register_tunnel(tunnel),
            Err(e) => error!("Could not acquire SOCKS tunnel table mutex: {}", e)
        }
    }

}

