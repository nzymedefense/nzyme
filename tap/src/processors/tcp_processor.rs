use std::{sync::{Arc}};
use std::sync::Mutex;
use log::error;

use crate::{ethernet::packets::TcpSegment};
use crate::data::tcp_table::TcpTable;

pub struct TcpProcessor {
    pub tcp_table: Arc<Mutex<TcpTable>>
}

impl TcpProcessor {

    pub fn new(tcp_table: Arc<Mutex<TcpTable>>) -> Self {
        Self{ tcp_table }
    }

    pub fn process(&mut self, segment: &Arc<TcpSegment>) {
        match self.tcp_table.lock() {
            Ok(mut table) => table.register_segment(segment),
            Err(e) => {
                error!("Could not acquire TCP table mutex: {}", e);
            }
        }
    }

}