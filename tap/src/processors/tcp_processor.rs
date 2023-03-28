use std::{sync::{Arc, Mutex}};

use log::error;

use crate::{data::l4_table::L4Table, ethernet::packets::TCPPacket};

pub struct TCPProcessor {
    l4_table: Arc<Mutex<L4Table>>
}

impl TCPProcessor {

    pub fn new(l4_table: Arc<Mutex<L4Table>>) -> Self {
        Self {
            l4_table
         }
    }

    pub fn process(&mut self, packet: &Arc<TCPPacket>) {
        match self.l4_table.lock() {
            Ok(mut table) => table.register_tcp_pair(packet),
            Err(e) => error!("Could not acquire L4 table mutex to register TCP packet: {}", e)
        }
    }

}