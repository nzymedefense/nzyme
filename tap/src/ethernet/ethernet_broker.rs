use std::panic::catch_unwind;
use std::{thread, sync::Arc};

use chrono::Utc;
use log::{debug, info, warn, error, trace};
use byteorder::{BigEndian, ByteOrder};
use crate::ethernet::packets::EthernetData;
use crate::ethernet::parsers::{udp_parser, tcp_parser};
use crate::messagebus::bus::Bus;
use crate::{to_pipeline, tracemark};
use crate::{helpers::network::to_mac_address_string, ethernet::types};
use crate::ethernet::{
    packets::EthernetPacket,
    types::{EtherType, find_ethertype},
    parsers::{ipv4_parser, ipv6_parser, arp_parser}
};
use crate::messagebus::channel_names::EthernetChannelName;

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
        if data.data.len() < 14 {
            tracemark!("Ethernet data too short.");
            return;
        }

        let packet_type = BigEndian::read_u16(&data.data[12..14]);
        let len = data.data.len();

        /*
         * If this is 802.1Q ("VLAN"), we have to parse the extra data, move the cursor ahead of it
         * and then overwrite the packet type with the original packet type to continue parsing.
         */
        let (packet_type, data_cursor) = if packet_type == 0x8100 {
            if data.data.len() < 18 {
                tracemark!("802.1Q data too short.");
                return;
            }

            // If we ever wanted to parse VLAN info, we'd do that here.

            (BigEndian::read_u16(&data.data[16..18]), 18)
        } else {
            // Not 802.1Q, leave unchanged.
            (packet_type, 14)
        };

        if data.data.len() < data_cursor {
            tracemark!("Ethernet data too short.");
            return;
        }

        let packet = Arc::new(EthernetPacket {
            destination_mac: to_mac_address_string(&data.data[0..6]),
            source_mac: to_mac_address_string(&data.data[6..12]),
            data: data.data[data_cursor..].to_vec(),
            packet_type: find_ethertype(packet_type),
            size: len as u32,
            timestamp: Utc::now()
        });

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
                                EthernetChannelName::TcpPipeline,
                                bus.tcp_pipeline.sender,
                                tcp,
                                size
                            );
                        },
                        types::ProtocolType::Udp  => {
                            let udp = Arc::new(udp_parser::parse(ipv4).unwrap()); // TODO unwrap
                            let size = udp.size;

                            // To UDP pipeline.
                            to_pipeline!(
                                EthernetChannelName::UdpPipeline,
                                bus.udp_pipeline.sender,
                                udp,
                                size
                            );
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
                        EthernetChannelName::ArpPipeline,
                        bus.arp_pipeline.sender,
                        Arc::new(packet),
                        size
                    );
                },
                Err(e) => { warn!("Could not parse ARP packet: {}", e) }
            };
        } else {
            debug!("Unknown packet type {}", packet_type)
        }
    }

}
