use std::sync::Arc;
use std::sync::Mutex;
use log::error;
use crate::context::context_engine::ContextEngine;
use crate::context::context_source::ContextSource;
use crate::state::tables::tcp_table::TcpTable;
use crate::wired::packets::TcpSegment;

pub struct TcpProcessor {
    tcp_table: Arc<Mutex<TcpTable>>,
    context: Arc<ContextEngine>
}

impl TcpProcessor {

    pub fn new(tcp_table: Arc<Mutex<TcpTable>>, context: Arc<ContextEngine>) -> Self {
        Self{ tcp_table, context }
    }

    pub fn process(&mut self, segment: Arc<TcpSegment>) {
        match self.tcp_table.lock() {
            Ok(mut table) => table.register_segment(&segment),
            Err(e) => {
                error!("Could not acquire TCP table mutex: {}", e);
            }
        }
        
        if let Some(source_mac) = &segment.source_mac {
            self.context.register_mac_address_ip(
                source_mac.clone(), segment.source_address, ContextSource::Tcp
            );
        }
    }

}