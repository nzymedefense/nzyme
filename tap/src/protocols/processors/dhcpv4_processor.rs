use std::sync::{Arc, Mutex};
use log::error;
use crate::context::context_engine::ContextEngine;
use crate::context::context_source::ContextSource;
use crate::wired::packets::Dhcpv4Packet;
use crate::wired::types::Dhcpv4MessageType::Request;
use crate::state::state::State;
use crate::state::tables::dhcp_table::DhcpTable;

pub struct Dhcpv4Processor {
    state: Arc<State>,
    table: Arc<Mutex<DhcpTable>>,
    context: Arc<ContextEngine>
}

impl Dhcpv4Processor {

    pub fn new(state: Arc<State>, table: Arc<Mutex<DhcpTable>>, context: Arc<ContextEngine>) -> Self {
        Self { state, table, context }
    }

    pub fn process(&mut self, packet: Arc<Dhcpv4Packet>) {
        // Add to DHCP table.
        match self.table.lock() {
            Ok(table) => {
                table.register_dhcpv4_packet(packet.clone())
            },
            Err(e) => error!("Could not acquire DHCP table lock: {}", e)
        }
        
        // Can we update transparent context with a hostname?
        if packet.message_type.eq(&Request) && packet.hostname.is_some() {
            self.context.register_mac_address_hostname(
                packet.client_mac_address.clone(),
                (*packet.hostname.as_ref().unwrap()).clone(),
                None,
                ContextSource::Dhcp
            )
        }
    }

}