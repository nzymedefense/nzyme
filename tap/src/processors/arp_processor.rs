use std::{sync::Arc, collections::HashMap};

use log::{debug, error};

use crate::{ethernet::{packets::ARPPacket, types::ARPOpCode}, data::tables::Tables};

pub struct ARPProcessor {
   tables: Arc<Tables>
}

impl ARPProcessor {

    pub fn new(tables: Arc<Tables>) -> Self {
        Self {
            tables
        }
    }

    pub fn process(&mut self, packet: &Arc<ARPPacket>) {
        match packet.operation {
            ARPOpCode::Request => { self.process_request(packet) },
            ARPOpCode::Reply => { self.process_reply(packet) },
            ARPOpCode::NotImplemented => {
                debug!("Received ARP packet with not implemented op code: {:?}", packet);
            }
        }
    }

    fn process_reply(&mut self, packet: &ARPPacket) {
        debug!("ARP reply: <{}/{}>, <{}> is at <{}>.",
            packet.target_protocol_address,
            packet.target_hardware_address,
            packet.sender_protocol_address,
            packet.sender_hardware_address);
   
        match self.tables.arp.lock() {
            Ok(mut table) => {

                if let Some(ips) = table.get_mut(&packet.sender_protocol_address) {
                    match ips.get_mut(&packet.sender_hardware_address) {
                        Some(count) => { *count += 1 },
                        None => {
                            ips.insert(packet.sender_hardware_address.clone(), 1);
                        }
                    }
                } else {
                    let mut mac = HashMap::new();
                    mac.insert(packet.sender_hardware_address.clone(), 1);
                    table.insert(packet.sender_protocol_address.clone(), mac);
                }
            },
            Err(e) => {
                error!("Could not acquire table mutex. {}", e);
            }
        }
    }

    // TODO remove linter hint when ARP is fully implemented (or fix what hint points out)
    #[allow(clippy::unused_self)]
    fn process_request(&mut self, packet: &ARPPacket) {
        if packet.sender_protocol_address == "00:00:00:00:00:00" {
            // Ignore ARP probes.
            return;
        }

        if (packet.target_protocol_address == packet.sender_protocol_address) 
            && (packet.target_hardware_address == "00:00:00:00:00:00" 
                || packet.target_hardware_address == packet.sender_hardware_address) {
            // ARP Announce. Currently not relevant, but log, then ignore it.
            debug!("ARP announce. {} is at {}.", packet.sender_protocol_address, packet.sender_hardware_address);
            return;
        }

        debug!("ARP request: Who has <{}>? Tell <{}>", packet.target_protocol_address, packet.sender_protocol_address);
    }

}
