use std::sync::{Arc, Mutex};

use crossbeam_channel::{Sender, Receiver, bounded};
use log::{debug, error};

use crate::{
    ethernet::packets::{
        ARPPacket,
        EthernetData,
        Datagram,
        DNSPacket,
        TcpSegment
    },
    metrics::Metrics, dot11::frames::{Dot11RawFrame, Dot11Frame}
};
use crate::bluetooth::bluetooth_device_advertisement::BluetoothDeviceAdvertisement;
use crate::configuration::Configuration;
use crate::ethernet::packets::{DHCPv4Packet, SocksTunnel, SshSession};
use crate::messagebus::channel_names::{BluetoothChannelName, Dot11ChannelName, EthernetChannelName};

pub struct Bus {
    pub name: String,

    pub ethernet_broker: NzymeChannel<EthernetData>,
    pub dot11_broker: NzymeChannel<Dot11RawFrame>,

    pub dot11_frames_pipeline: NzymeChannel<Dot11Frame>,

    pub bluetooth_device_pipeline: NzymeChannel<BluetoothDeviceAdvertisement>,

    pub arp_pipeline: NzymeChannel<ARPPacket>,
    pub tcp_pipeline: NzymeChannel<TcpSegment>,
    pub udp_pipeline: NzymeChannel<Datagram>,
    pub dns_pipeline: NzymeChannel<DNSPacket>,
    pub ssh_pipeline: NzymeChannel<SshSession>,
    pub socks_pipeline: NzymeChannel<SocksTunnel>,
    pub dhcpv4_pipeline: NzymeChannel<DHCPv4Packet>
}

pub struct NzymeChannelSender<T> {
    sender: Sender<Arc<T>>,
    name: String,
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

    pub fn new(metrics: Arc<Mutex<Metrics>>, name: String, configuration: Configuration) -> Self {
        let (ethernet_broker_sender, ethernet_broker_receiver) = 
            bounded(configuration.performance.ethernet_broker_buffer_capacity);
        let (dot11_broker_sender, dot11_broker_receiver) = 
            bounded(configuration.performance.wifi_broker_buffer_capacity);

        let (dot11_frames_sender, dot11_frames_receiver) = 
            bounded(configuration.performance.wifi_broker_buffer_capacity);

        let (bluetooth_devices_sender, bluetooth_devices_receiver) =
            bounded(configuration.performance.bluetooth_devices_pipeline_size
                .unwrap_or(1024) as usize);

        let (arp_pipeline_sender, arp_pipeline_receiver) = 
            bounded(configuration.protocols.arp.pipeline_size as usize);
        let (tcp_pipeline_sender, tcp_pipeline_receiver) = 
            bounded(configuration.protocols.tcp.pipeline_size as usize);
        let (udp_pipeline_sender, udp_pipeline_receiver) = 
            bounded(configuration.protocols.udp.pipeline_size as usize);
        let (dns_pipeline_sender, dns_pipeline_receiver) = 
            bounded(configuration.protocols.dns.pipeline_size as usize);
        let (socks_pipeline_sender, socks_pipeline_receiver) =
            bounded(configuration.protocols.socks.pipeline_size as usize);
        let (ssh_pipeline_sender, ssh_pipeline_receiver) =
            bounded(configuration.protocols.ssh.pipeline_size as usize);
        let (dhcpv4_pipeline_sender, dhcpv4_pipeline_receiver) =
            bounded(configuration.protocols.dhcpv4.pipeline_size as usize);
        Self {
            name,
            ethernet_broker: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),
                    sender: ethernet_broker_sender,
                    name: EthernetChannelName::EthernetBroker.to_string()
                }),
                receiver: Arc::new(ethernet_broker_receiver),
            },
            dot11_broker: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),
                    sender: dot11_broker_sender,
                    name: Dot11ChannelName::Dot11Broker.to_string()
                }),
                receiver: Arc::new(dot11_broker_receiver),
            },
            dot11_frames_pipeline: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),
                    sender: dot11_frames_sender,
                    name: Dot11ChannelName::Dot11FramesPipeline.to_string()
                }),
                receiver: Arc::new(dot11_frames_receiver),
            },
            bluetooth_device_pipeline: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),
                    sender: bluetooth_devices_sender,
                    name: BluetoothChannelName::BluetoothDevicesPipeline.to_string()
                }),
                receiver: Arc::new(bluetooth_devices_receiver),
            },
            arp_pipeline: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),  
                    sender: arp_pipeline_sender,
                    name: EthernetChannelName::ArpPipeline.to_string()
                }),
                receiver: Arc::new(arp_pipeline_receiver),
            },
            tcp_pipeline: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),
                    sender: tcp_pipeline_sender,
                    name: EthernetChannelName::TcpPipeline.to_string()
                }),
                receiver: Arc::new(tcp_pipeline_receiver),
            },
            udp_pipeline: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),
                    sender: udp_pipeline_sender,
                    name: EthernetChannelName::UdpPipeline.to_string()
                }),
                receiver: Arc::new(udp_pipeline_receiver),
            },
            dns_pipeline: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),
                    sender: dns_pipeline_sender,
                    name: EthernetChannelName::DnsPipeline.to_string()
                }),
                receiver: Arc::new(dns_pipeline_receiver),
            },
            socks_pipeline: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),
                    sender: socks_pipeline_sender,
                    name: EthernetChannelName::SocksPipeline.to_string()
                }),
                receiver: Arc::new(socks_pipeline_receiver),
            },
            ssh_pipeline: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics: metrics.clone(),
                    sender: ssh_pipeline_sender,
                    name: EthernetChannelName::SshPipeline.to_string()
                }),
                receiver: Arc::new(ssh_pipeline_receiver),
            },
            dhcpv4_pipeline: NzymeChannel {
                sender: Mutex::new(NzymeChannelSender {
                    metrics,
                    sender: dhcpv4_pipeline_sender,
                    name: EthernetChannelName::Dhcpv4Pipeline.to_string()
                }),
                receiver: Arc::new(dhcpv4_pipeline_receiver),
            }
        }
    }

}
