use std::sync::{Arc, Mutex};

use log::error;

use crate::{ethernet::packets::UDPPacket, data::l4_table::L4Table};

pub struct UDPProcessor {
    l4_table: Arc<Mutex<L4Table>>
}

impl UDPProcessor {

    pub fn new(l4_table: Arc<Mutex<L4Table>>) -> Self {
        Self {
            l4_table
         }
    }

    pub fn process(&mut self, packet: &Arc<UDPPacket>) {
        match self.l4_table.lock() {
            Ok(mut table) => table.register_udp_pair(packet),
            Err(e) => error!("Could not acquire L4 table mutex to register UDP packet: {}", e)
        }
    }

}