use std::sync::{Arc, Mutex};
use log::error;

use crate::{ethernet::packets::Datagram, to_pipeline};
use crate::data::tables::Tables;
use crate::data::udp_table::UdpTable;
use crate::ethernet::detection::l7_tagger::L7SessionTag;
use crate::ethernet::detection::l7_tagger::L7SessionTag::{Dns, Unencrypted};
use crate::ethernet::parsers::dns_parser;
use crate::messagebus::bus::Bus;
use crate::messagebus::channel_names::EthernetChannelName;

pub struct UDPProcessor {
    bus: Arc<Bus>,
    udp_table: Arc<Mutex<UdpTable>>
}

impl UDPProcessor {

    pub fn new(bus: Arc<Bus>, udp_table: Arc<Mutex<UdpTable>>) -> Self {
        Self { bus, udp_table }
    }

    pub fn process(&mut self, datagram: &Arc<Datagram>) {
        let tags = self.tag_and_route(datagram);
        match datagram.tags.lock() {
            Ok(mut t) => t.extend(tags),
            Err(e) => error!("Could not extend tags of datagram: {}", e)
        }

        // Register in UDP table.
        match self.udp_table.lock() {
            Ok(mut table) => table.register_datagram(datagram),
            Err(e) => {
                error!("Could not acquire UDP table mutex: {}", e);
            }
        }
    }

    fn tag_and_route(&mut self, datagram: &Arc<Datagram>) -> Vec<L7SessionTag> {
        let mut tags = vec![];

        match dns_parser::parse(datagram) {
            Ok(dns) => {},
            Err(e) => {}
        }

        /*if let Ok(dns) = dns_parser::parse(datagram) {
            //tags.push(Dns);
            //tags.push(Unencrypted);

            // To DNS pipeline.
            //let size = dns.size;
            /*to_pipeline!(
                EthernetChannelName::DnsPipeline,
                self.bus.dns_pipeline.sender,
                Arc::new(dns),
                size
            );*/
        }*/

        tags
    }

}