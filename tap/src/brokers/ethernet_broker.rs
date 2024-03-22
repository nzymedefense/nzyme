use std::panic::catch_unwind;
use std::{thread, sync::Arc};

use chrono::Utc;
use log::{debug, info, warn, error};
use byteorder::{BigEndian, ByteOrder};
use crate::ethernet::packets::EthernetData;
use crate::ethernet::parsers::{udp_parser, dns_parser, tcp_parser};
use crate::messagebus::bus::Bus;
use crate::messagebus::channel_names::ChannelName;
use crate::to_pipeline;
use crate::{helpers::network::to_mac_address_string, ethernet::types};
use crate::ethernet::{
    packets::EthernetPacket,
    types::{EtherType, find_ethertype},
    parsers::{ipv4_parser, ipv6_parser, arp_parser}
};

pub struct EthernetBroker {
    num_threads: usize,
    bus: Arc<Bus>
}

impl EthernetBroker {

    pub fn new(bus: Arc<Bus>, num_threads: usize) -> Self {
        Self {
            num_threads,
            bus
        }
    }

    pub fn run(&mut self) {
        for num in 0..self.num_threads {
            info!("Installing ethernet broker thread <{}>.", num);
           
            let receiver = self.bus.ethernet_broker.receiver.clone();
            let bus = self.bus.clone();
            thread::spawn(move || {
                for packet in receiver.iter() {
                    let handler_result = catch_unwind(|| {
                        Self::handle(&packet, &bus)
                    });

                    if handler_result.is_err() {
                        error!("Unexpected error in packet handling. Skipping.");
                    }
                }
            });
        }
    }

    #[allow(clippy::cast_possible_truncation)]
    fn handle(data: &Arc<EthernetData>, bus: &Arc<Bus>) {
        let packet_type = BigEndian::read_u16(&data.data[12..14]);
        let len = data.data.len();

        let packet = Arc::new(EthernetPacket {
            destination_mac: to_mac_address_string(&data.data[0..6]),
            source_mac: to_mac_address_string(&data.data[6..12]),
            data: data.data[14..len].to_vec(),
            packet_type: find_ethertype(packet_type),
            size: len as u32,
            timestamp: Utc::now()
        }); 

        // To Ethernet Pipeline.
        to_pipeline!(
            ChannelName::EthernetPipeline,
            bus.ethernet_pipeline.sender,
            packet.clone(),
            packet.data.len() as u32
        );

        if packet.packet_type == EtherType::IPv4 {
            let ipv4 = match ipv4_parser::parse(&packet) {
                Ok(x) => x,
                Err(err) => {
                    warn!("Could not parse IPv4 packet: {}", err);
                    return;
                }
            };

            match types::ProtocolType::try_from(ipv4.protocol) {
                Ok(protocol_type) => { 
                    match protocol_type {
                        types::ProtocolType::Tcp  => {
                            let tcp = Arc::new(tcp_parser::parse(ipv4).unwrap()); // TODO unwrap
                            let size = tcp.size;

                            // To TCP pipeline.
                            to_pipeline!(
                                ChannelName::TcpPipeline,
                                bus.tcp_pipeline.sender,
                                tcp,
                                size
                            );
                        },
                        types::ProtocolType::Udp  => {
                            let udp = Arc::new(udp_parser::parse(ipv4).unwrap()); // TODO unwrap
                            let size = udp.size;

                            // DNS
                            if udp.destination_port == 53 || udp.source_port == 53 { // TODO configurable
                                match dns_parser::parse(&udp) {
                                    Ok(dns) => {
                                        // To DNS pipeline.
                                        let size = dns.size;
                                        to_pipeline!(
                                            ChannelName::DnsPipeline,
                                            bus.dns_pipeline.sender,
                                            Arc::new(dns),
                                            size
                                        );
                                    },
                                    Err(e) => warn!("Could not parse DNS packet: {}", e)
                                };
                            }

                            // To UDP pipeline. TODO re-enable
                            /*to_pipeline!(
                                ChannelName::UdpPipeline,
                                bus.udp_pipeline.sender,
                                udp,
                                size
                            );*/
                        },
                        types::ProtocolType::Icmp6 |
                        types::ProtocolType::Icmp => {}
                    }
                },
                Err(..) => { 
                    debug!("IPv4 type not implemented: {}", ipv4.protocol);
                }
            }
        } else if packet.packet_type == EtherType::IPv6 {
            let _ip6 = ipv6_parser::parse(&packet);
        } else if packet.packet_type == EtherType::Arp {
            match arp_parser::parse(&packet) {
                Ok(packet) => {
                    let size = packet.size;
                    to_pipeline!(
                        ChannelName::ArpPipeline,
                        bus.arp_pipeline.sender,
                        Arc::new(packet),
                        size
                    );
                },
                Err(e) => { warn!("Could not parse ARP packet: {}", e) }
            };
        } else {
        }
    }

}
