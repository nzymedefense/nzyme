use std::sync::{Arc, Mutex};
use log::error;
use crate::context::context_engine::ContextEngine;
use crate::context::context_source::ContextSource;
use crate::to_pipeline;
use crate::protocols::detection::l7_tagger::L7SessionTag;
use crate::protocols::detection::l7_tagger::L7SessionTag::{Dhcpv4, Dns, Unencrypted};
use crate::protocols::parsers::{dhcpv4_parser, dns_parser};
use crate::state::tables::udp_table::UdpTable;
use crate::helpers::timer::{record_timer, Timer};
use crate::messagebus::bus::Bus;
use crate::messagebus::channel_names::WiredChannelName;
use crate::metrics::Metrics;
use crate::wired::packets::Datagram;

pub struct UDPProcessor {
    bus: Arc<Bus>,
    metrics: Arc<Mutex<Metrics>>,
    udp_table: Arc<Mutex<UdpTable>>,
    context: Arc<ContextEngine>
}

impl UDPProcessor {

    pub fn new(bus: Arc<Bus>, 
               metrics: Arc<Mutex<Metrics>>, 
               udp_table: Arc<Mutex<UdpTable>>, 
               context: Arc<ContextEngine>) -> Self {
        Self { bus, metrics, udp_table, context }
    }

    pub fn process(&mut self, datagram: Arc<Datagram>) {
        let mut timer = Timer::new();
        let tags = self.tag_and_route(&datagram);
        timer.stop();
        record_timer(
            timer.elapsed_microseconds(),
            "processors.udp.timer.tag_and_route",
            &self.metrics
        );

        match datagram.tags.lock() {
            Ok(mut t) => t.extend(tags),
            Err(e) => error!("Could not extend tags of datagram: {}", e)
        }

        // Register in UDP table.
        match self.udp_table.lock() {
            Ok(mut table) => table.register_datagram(datagram.clone()),
            Err(e) => {
                error!("Could not acquire UDP table mutex: {}", e);
            }
        }

        if let Some(source_mac) = &datagram.source_mac {
            self.context.register_mac_address_ip(
                source_mac.clone(),
                datagram.source_address,
                ContextSource::Udp
            );
        }
    }

    fn tag_and_route(&mut self, datagram: &Arc<Datagram>) -> Vec<L7SessionTag> {
        let mut tags = vec![];

        if let Some(dns) = dns_parser::parse(datagram) {
            tags.push(Dns);
            tags.push(Unencrypted);
            
            // To DNS pipeline.
            let size = dns.size;
            to_pipeline!(
                WiredChannelName::DnsPipeline,
                self.bus.dns_pipeline.sender,
                Arc::new(dns),
                size
            );
        }

        if let Some(dhcp) = dhcpv4_parser::parse(datagram) {
            tags.push(Dhcpv4);
            tags.push(Unencrypted);

            // To DHCPv4 pipeline.
            let size = dhcp.estimate_struct_size();
            to_pipeline!(
                WiredChannelName::Dhcpv4Pipeline,
                self.bus.dhcpv4_pipeline.sender,
                Arc::new(dhcp),
                size
            );
        }

        tags
    }

}