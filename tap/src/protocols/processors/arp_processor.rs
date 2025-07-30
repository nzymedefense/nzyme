use std::{sync::Arc};
use std::net::{IpAddr, Ipv4Addr};
use log::{debug, error};
use crate::state::tables::tables::Tables;
use crate::context::context_engine::ContextEngine;
use crate::context::context_source::ContextSource;
use crate::state::state::State;
use crate::wired::packets::ArpPacket;
use crate::wired::types::ArpOpCode;

pub struct ARPProcessor {
    tables: Arc<Tables>,
    state: Arc<State>,
    context: Arc<ContextEngine>
}

impl ARPProcessor {

    pub fn new(tables: Arc<Tables>,
               state: Arc<State>,
               context: Arc<ContextEngine>) -> Self {
        Self { tables, state, context }
    }

    pub fn process(&mut self, packet: Arc<ArpPacket>) {
        match packet.operation {
            ArpOpCode::Request => { self.process_request(packet) },
            ArpOpCode::Reply => { self.process_reply(packet) },
            ArpOpCode::NotImplemented => {
                debug!("Received ARP packet with not implemented op code: {:?}", packet);
            }
        }
    }

    fn process_reply(&mut self, packet: Arc<ArpPacket>) {
        debug!("ARP reply: <{}/{}>, <{}> is at <{}>.",
            packet.arp_target_address,
            packet.arp_target_mac,
            packet.arp_sender_address,
            packet.arp_sender_mac);
        
        self.process_discovered_ip(packet.arp_sender_mac.clone(), packet.arp_sender_address);
        self.process_discovered_ip(packet.arp_target_mac.clone(), packet.arp_target_address);

        match self.tables.arp.lock() {
            Ok(mut table) => table.register_reply(packet.clone()),
            Err(e) => {
                error!("Could not acquire table mutex. {}", e);
            }
        }
    }

    fn process_request(&mut self, packet: Arc<ArpPacket>) {
        match self.tables.arp.lock() {
            Ok(mut table) => table.register_request(packet.clone()),
            Err(e) => {
                error!("Could not acquire table mutex. {}", e);
            }
        }

        if (packet.arp_target_address == packet.arp_sender_address)
            && (packet.arp_target_mac == "00:00:00:00:00:00"
                || packet.arp_target_mac == packet.arp_sender_mac) {
            // ARP broadcast announce.
            debug!("ARP announce. {} is at {}.", packet.arp_sender_address, packet.arp_sender_mac);
            self.process_discovered_ip(packet.arp_sender_mac.clone(), packet.arp_sender_address);
        } else {
            debug!("ARP request: Who has <{}>? Tell <{}/{}>",
            packet.arp_target_address, packet.arp_sender_address, packet.arp_sender_mac);
            self.process_discovered_ip(packet.arp_sender_mac.clone(), packet.arp_sender_address);
        }
    }

    fn process_discovered_ip(&self, mac: String, ip: IpAddr) {
        // Update context of MAC address.
        self.context.register_mac_address_ip(mac.clone(), ip, ContextSource::Arp);

        // Update ARP state.
        match self.state.arp.lock() {
            Ok(mut state) => state.update(mac, ip),
            Err(e) => error!("Could not acquire ARP state mutex: {}", e)
        }
    }

}
