use std::sync::{Arc, Mutex};

use crossbeam_channel::{Sender, Receiver, bounded};
use log::{debug, error};

use crate::{
    ethernet::packets::{
        EthernetPacket,
        ARPPacket,
        EthernetData,
        UDPPacket,
        DNSPacket,
        TCPPacket
    },
    metrics::Metrics, dot11::frames::Dot11Frame
};

use super::channel_names::ChannelName;

pub struct Bus {
    pub name: String,

    pub ethernet_broker: NzymeChannel<EthernetData>,
    pub dot11_broker: NzymeChannel<Dot11Frame>,

    pub ethernet_pipeline: NzymeChannel<EthernetPacket>,
    pub arp_pipeline: NzymeChannel<ARPPacket>,
    pub tcp_pipeline: NzymeChannel<TCPPacket>,
    pub udp_pipeline: NzymeChannel<UDPPacket>,
    pub dns_pipeline: NzymeChannel<DNSPacket>
}

pub struct NzymeChannelSender<T> {
    sender: Sender<Arc<T>>,
    name: ChannelName,
    metrics: Arc<Mutex<Metrics>>
}

pub struct NzymeChannel<T> {
    pub sender: Mutex<NzymeChannelSender<T>>,
    pub receiver: Arc<Receiver<Arc<T>>>,
}

impl<T> NzymeChannelSender<T> {

    pub fn send_packet(&mut self, packet: Arc<T>, packet_length: u32) {
        if let Err(err) = self.sender.try_send(packet) {
            debug!("Could not write to channel [{:?}]: {}", self.name, err);
            
            match self.metrics.lock() {
                Ok(mut metrics) => {
                    metrics.increment_channel_errors(&self.name, 1);
                },
                Err(e) => { error!("Could not acquire metrics mutex: {}", e) }
            }
        } else {
            // Record metrics.
            match self.metrics.lock() {
                Ok(mut metrics) => {
                    metrics.record_channel_capacity(&self.name, self.sender.capacity().unwrap() as u128);
                    metrics.record_channel_watermark(&self.name, self.sender.len() as u128);
                    metrics.increment_channel_throughput_messages(&self.name, 1);
                    metrics.increment_channel_throughput_bytes(&self.name, packet_length);
                },
                Err(e) => { error!("Could not acquire metrics mutex: {}", e) }
            }
        }
    }

}

impl Bus<> {

    pub fn new(metrics: Arc<Mutex<Metrics>>, name: String) -> Self {
        let (ethernet_broker_sender, ethernet_broker_receiver) = bounded(65536); // TODO configurable
        let (dot11_broker_sender, dot11_broker_receiver) = bounded(65536); // TODO configurable

        let (ethernet_pipeline_sender, ethernet_pipeline_receiver) = bounded(65536); // TODO configurable
        let (arp_pipeline_sender, arp_pipeline_receiver) = bounded(512); // TODO configurable

        let (tcp_pipeline_sender, tcp_pipeline_receiver) = bounded(512); // TODO configurable
        let (udp_pipeline_sender, udp_pipeline_receiver) = bounded(512); // TODO configurable

        let (dns_pipeline_sender, dns_pipeline_receiver) = bounded(512); // TODO configurable

        Self {
            name,
            ethernet_broker: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),
                    sender: ethernet_broker_sender,
                    name: ChannelName::EthernetBroker
                }),
                receiver: Arc::new(ethernet_broker_receiver),
            },
            dot11_broker: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),
                    sender: dot11_broker_sender,
                    name: ChannelName::Dot11Broker
                }),
                receiver: Arc::new(dot11_broker_receiver),
            },
            ethernet_pipeline: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),
                    sender: ethernet_pipeline_sender,
                    name: ChannelName::EthernetPipeline 
                }),
                receiver: Arc::new(ethernet_pipeline_receiver),
            },
            arp_pipeline: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),  
                    sender: arp_pipeline_sender,
                    name: ChannelName::ArpPipeline 
                }),
                receiver: Arc::new(arp_pipeline_receiver),
            },
            tcp_pipeline: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),
                    sender: tcp_pipeline_sender,
                    name: ChannelName::TcpPipeline 
                }),
                receiver: Arc::new(tcp_pipeline_receiver),
            },
            udp_pipeline: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),
                    sender: udp_pipeline_sender,
                    name: ChannelName::UdpPipeline 
                }),
                receiver: Arc::new(udp_pipeline_receiver),
            },
            dns_pipeline: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics,
                    sender: dns_pipeline_sender,
                    name: ChannelName::DnsPipeline 
                }),
                receiver: Arc::new(dns_pipeline_receiver),
            },
        }
    }

}
