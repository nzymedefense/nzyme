/*use std::sync::{Arc, Mutex};

use log::{error, info};

use crate::{
    messagebus::bus::Bus,
    metrics::Metrics,
};

pub struct Capture {
    pub metrics: Arc<Mutex<Metrics>>,
    pub bus: Arc<Bus>
}

impl Capture<> {

    // TODO remove linter hint
    #![allow(clippy::unused_self)]
    pub fn run(&mut self, device_name: &str) {
        info!("Starting WiFi capture on [{}]", device_name);

        let device = match pcap::Capture::from_device(device_name) {
            Ok(device) => {
                device
                    .immediate_mode(true)
                    .promisc(true)
            },
            Err(e) => {
                error!("Could not get PCAP device handle on [{}]: {}", device_name, e);
                return;
            }
        };

        let mut handle = match device.open() {
            Ok(handle) => handle,
            Err(e) => {
                error!("Could not get PCAP capture handle on [{}]: {}", device_name, e);
                return;
            }
        };

        if let Err(e) = handle.set_datalink(pcap::Linktype::IEEE802_11_RADIOTAP) {
            error!("Could not set datalink type on [{}]: {}", device_name, e);
            return;
        }

        if let Err(e) = handle.filter("", true) {
            error!("Could not set filter on [{}]: {}", device_name, e);
            return;
        }

        let stats = handle.stats();

        loop {
            let packet = match handle.next_packet() {
                Ok(packet) => packet,
                Err(e) => {
                    error!("Ethernet capture exception: {}", e);
                    continue;
                }
            };

        }
    }
}*/