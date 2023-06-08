use std::sync::{Arc, Mutex};

use log::{info, debug, error};
use pcap::Device;

use crate::{
    messagebus::bus::Bus,
    metrics::Metrics, ethernet::packets::EthernetData
};

pub struct Capture {
    pub metrics: Arc<Mutex<Metrics>>,
    pub bus: Arc<Bus>
}

impl Capture<> {

    #[allow(clippy::cast_possible_truncation)]
    pub fn run(&mut self, device_name: &str) {
        info!("Starting ethernet capture on [{}].", &device_name);

        let device = match pcap::Capture::from_device(device_name) {
            Ok(device) => {
                device
                    .immediate_mode(true)
                    .promisc(true)
            },
            Err(e) => {
                error!("Could not get PCAP device handle on [{}]: {}", &device_name, e);
                return;
            }
        };

        let mut handle = match device.open() {
            Ok(handle) => handle,
            Err(e) => {
                error!("Could not get PCAP capture handle on [{}]: {}", &device_name, e);
                return;
            }
        };

        if let Err(e) = handle.set_datalink(pcap::Linktype::ETHERNET) {
            error!("Could not set datalink type on [{}]: {}", &device_name, e);
            return;
        }

        if let Err(e) = handle.filter("ether proto (\\arp or \\ip or \\ip6)", true) {
            error!("Could not set filter on [{}]: {}", &device_name, e);
            return;
        }

        let stats = handle.stats();

        loop {
            let packet = match handle.next_packet() {
                Ok(packet) => packet,
                Err(e) => {
                    debug!("Ethernet capture exception: {}", e);
                    continue;
                }
            };

            let len = packet.data.len();

            match self.metrics.lock() {
                Ok(mut metrics) => {
                    match stats {
                        Ok(stats) => {
                            metrics.increment_processed_bytes_total(len as u32);
                            metrics.update_capture(device_name, true, stats.dropped, stats.if_dropped);
                        },
                        Err(ref e) => { // TOOD add error
                            error!("Could not fetch handle stats for capture [{}] metrics update: {}", device_name, e);
                        }
                    }
                },
                Err(e) => error!("Could not acquire metrics mutex: {}", e)
            }

            if packet.len() < 15 {
                debug!("Packet too small. Wouldn't even fit Ethernet header. Skipping.");
                continue;
            }

            
            let data = EthernetData {
                data: packet.data.to_vec()
            };

            // Write to Ethernet broker pipeline.
            match self.bus.ethernet_broker.sender.lock() {
                Ok(mut sender) => { sender.send_packet(Arc::new(data), len as u32) },
                Err(e) => error!("Could not aquire ethernet broker channel mutex: {}", e)
            }
        }
    }

}

pub fn print_devices() {
    match Device::list() {
        Ok(devices) => {
            for device in devices {
                let description = match device.desc {
                    Some(description) => description,
                    None => "no description".to_string()
                };

                let addresses = if device.addresses.is_empty() {
                    "no interface addresses".to_string()
                } else {
                    let mut collected = Vec::new();
                    for address in device.addresses {
                        collected.push(address.addr.to_string());
                    }

                    collected.join(", ")
                };

                info!("Available network device: {} ({}): [{}]", device.name, description, addresses);
            }
        },
        Err(e) => {
            error!("Could not get list of available network devices: {}", e);
        }
    }
}