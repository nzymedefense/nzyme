use std::sync::{Arc, Mutex};

use caps::{CapSet, Capability};
use log::{debug, error, info};

use crate::{
    messagebus::bus::Bus,
    metrics::Metrics,
};
use crate::wireless::dot11::nl::InterfaceState::{Down, Up};
use crate::wireless::dot11::nl::Nl;
use crate::wireless::dot11::frames::Dot11RawFrame;

pub struct Capture {
    pub metrics: Arc<Mutex<Metrics>>,
    pub bus: Arc<Bus>
}

impl Capture {

    pub fn run(&mut self, device_name: &str) {
        info!("Starting WiFi capture on [{}]", device_name);

        // Check if `net_admin` permission is set on this program.
        let permission = caps::has_cap(None, CapSet::Permitted, Capability::CAP_NET_ADMIN);
        match permission {
            Ok(result) => {
                if !result {
                    error!("Missing `net_admin` permission on this program. Please follow the documentation.");
                    return;
                }
            }
            Err(e) => {
                error!("Could not check program capabilities: {}", e);
                return;
            }
        }

        let mut nl = match Nl::new() {
            Ok(nl) => nl,
            Err(e) => {
                error!("Could not establish Netlink connection: {}", e);
                return
            }
        };

        info!("Temporarily disabling interface [{}] ...", device_name);
        match nl.change_80211_interface_state(&device_name.to_string(), Down) {
            Ok(_) => info!("Device [{}] is now down.", device_name),
            Err(e) => {
                error!("Could not disable device [{}]: {}", device_name, e);
                return;
            }
        }

        info!("Enabling monitor mode on interface [{}] ...", device_name);
        match nl.enable_monitor_mode(&device_name.to_string()) {
            Ok(_) => info!("Device [{}] is now in monitor mode.", device_name),
            Err(e) => {
                error!("Could not set device [{}] to monitor mode: {}", device_name, e);
                return;
            }
        }

        info!("Enabling interface [{}] ...", device_name);
        match nl.change_80211_interface_state(&device_name.to_string(), Up) {
            Ok(_) => info!("Device [{}] is now up.", device_name),
            Err(e) => {
                error!("Could not enable device [{}]: {}", device_name, e);
                return;
            }
        }

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
            let frame = match handle.next_packet() {
                Ok(packet) => packet,
                Err(e) => {
                    error!("Dot11 capture exception: {}", e);
                    continue;
                }
            };

            let length = frame.data.len();

            match self.metrics.lock() {
                Ok(mut metrics) => {
                    match stats {
                        Ok(stats) => {
                            metrics.increment_processed_bytes_total(length as u32);
                            metrics.update_capture(device_name, true, stats.dropped, stats.if_dropped);
                        },
                        Err(ref e) => { // TOOD add error
                            error!("Could not fetch handle stats for capture [{}] metrics update: {}", device_name, e);
                        }
                    }
                },
                Err(e) => error!("Could not acquire metrics mutex: {}", e)
            }

            if length < 4 {
                debug!("Packet too small. Wouldn't even fit radiotap length information. Skipping.");
                continue;
            }

            let data = Dot11RawFrame {
                interface_name: device_name.to_string(),
                data: frame.data.to_vec()
            };
        
            // Write to Dot11 broker pipeline.
            match self.bus.dot11_broker.sender.lock() {
                Ok(mut sender) => { sender.send_packet(Arc::new(data), length as u32) },
                Err(e) => error!("Could not aquire dot11 handler broker mutex: {}", e)
            }
        }
    }
}