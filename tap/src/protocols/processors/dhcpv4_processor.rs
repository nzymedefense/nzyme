use std::sync::Arc;
use crate::context::context_engine::ContextEngine;
use crate::context::context_source::ContextSource;
use crate::wired::packets::DHCPv4Packet;
use crate::wired::types::DHCPv4MessageType::Request;
use crate::state::state::State;

pub struct Dhcpv4Processor {
    state: Arc<State>,
    context: Arc<ContextEngine>
}

impl Dhcpv4Processor {

    pub fn new(state: Arc<State>, context: Arc<ContextEngine>) -> Self {
        Self { state, context }
    }

    pub fn process(&mut self, packet: Arc<DHCPv4Packet>) {
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