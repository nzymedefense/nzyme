use std::sync::{Arc};
use log::error;

use crate::{ethernet::packets::Datagram, to_pipeline};
use crate::ethernet::detection::l7_tagger::L7SessionTag::{Dns, Unencrypted};
use crate::ethernet::parsers::dns_parser;
use crate::messagebus::bus::Bus;
use crate::messagebus::channel_names::EthernetChannelName;

pub struct UDPProcessor {
    bus: Arc<Bus>
}

impl UDPProcessor {

    pub fn new(bus: Arc<Bus>) -> Self {
        Self { bus }
    }

    pub fn process(&mut self, datagram: &Arc<Datagram>) {
        let mut tags = vec![];

        if let Ok(dns) = dns_parser::parse(datagram) {
            tags.push(Dns);
            tags.push(Unencrypted);

            // To DNS pipeline.
            let size = dns.size;
            to_pipeline!(
                EthernetChannelName::DnsPipeline,
                self.bus.dns_pipeline.sender,
                Arc::new(dns),
                size
            );
        }

    }

}