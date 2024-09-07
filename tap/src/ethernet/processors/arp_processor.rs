use std::{sync::Arc, collections::HashMap};
use std::net::{IpAddr, Ipv4Addr};
use log::{debug, error};

use crate::{ethernet::{packets::ARPPacket, types::ARPOpCode}, tables::tables::Tables};
use crate::context::context_engine::ContextEngine;
use crate::context::context_source::ContextSource;
use crate::state::state::State;

pub struct ARPProcessor {
    tables: Arc<Tables>,
    state: Arc<State>,
    context: Arc<ContextEngine>
}

impl ARPProcessor {

    pub fn new(tables: Arc<Tables>, state: Arc<State>, context: Arc<ContextEngine>) -> Self {
        Self { tables, state, context }
    }

    pub fn process(&mut self, packet: Arc<ARPPacket>) {
        match packet.operation {
            ARPOpCode::Request => { self.process_request(packet) },
            ARPOpCode::Reply => { self.process_reply(packet) },
            ARPOpCode::NotImplemented => {
                debug!("Received ARP packet with not implemented op code: {:?}", packet);
            }
        }
    }

    fn process_reply(&mut self, packet: Arc<ARPPacket>) {
        debug!("ARP reply: <{}/{}>, <{}> is at <{}>.",
            packet.target_address,
            packet.target_mac_address,
            packet.sender_address,
            packet.sender_mac_address);
        
        self.process_discovered_ip(packet.sender_mac_address.clone(), packet.sender_address);
        self.process_discovered_ip(packet.target_mac_address.clone(), packet.target_address);

        match self.tables.arp.lock() {
            Ok(mut table) => {

                if let Some(ips) = table.get_mut(&packet.sender_address) {
                    match ips.get_mut(&packet.sender_mac_address) {
                        Some(count) => { *count += 1 },
                        None => {
                            ips.insert(packet.sender_mac_address.clone(), 1);
                        }
                    }
                } else {
                    let mut mac = HashMap::new();
                    mac.insert(packet.sender_mac_address.clone(), 1);
                    table.insert(packet.sender_address.clone(), mac);
                }
            },
            Err(e) => {
                error!("Could not acquire table mutex. {}", e);
            }
        }
    }

    fn process_request(&mut self, packet: Arc<ARPPacket>) {
        if packet.sender_address == IpAddr::V4(Ipv4Addr::UNSPECIFIED) {
            // Ignore ARP probes.
            return;
        }

        if (packet.target_address == packet.sender_address)
            && (packet.target_mac_address == "00:00:00:00:00:00"
                || packet.target_mac_address == packet.sender_mac_address) {
            // ARP broadcast announce.
            debug!("ARP announce. {} is at {}.", packet.sender_address, packet.sender_mac_address);
            self.process_discovered_ip(packet.sender_mac_address.clone(), packet.sender_address);
            return;
        }

        debug!("ARP request: Who has <{}>? Tell <{}/{}>",
            packet.target_address, packet.sender_address, packet.sender_mac_address);
        self.process_discovered_ip(packet.sender_mac_address.clone(), packet.sender_address);
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
