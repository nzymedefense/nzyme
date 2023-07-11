use std::{sync::{Arc, Mutex}, process::Command};

use caps::{CapSet, Capability};
use log::{error, info, debug};

use crate::{
    messagebus::bus::Bus,
    metrics::Metrics, dot11::frames::Dot11RawFrame,
};

pub struct Capture {
    pub metrics: Arc<Mutex<Metrics>>,
    pub bus: Arc<Bus>
}

impl Capture {

    pub fn run(&mut self, device_name: &str, cmd_ip: &str, cmd_iw: &str) {
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
                error!("Could not check program capabitilies: {}", e);
                return;
            }
        }

        // Raise ambient permissions to make spawned processes work with program capabilities.
        match caps::raise(None, caps::CapSet::Inheritable, caps::Capability::CAP_NET_ADMIN) {
            Ok(_) => {},
            Err(e) => {
                error!("Could not raise ambient program capabitilies: {}", e);
                return;
            }
        }
        match caps::raise(None, caps::CapSet::Ambient, caps::Capability::CAP_NET_ADMIN) {
            Ok(_) => {},
            Err(e) => {
                error!("Could not raise ambient program capabitilies: {}", e);
                return;
            }
        }

        info!("Temporarily disabling interface [{}] ...", device_name);

        let cmd_device_down = Command::new(cmd_ip)
            .arg("link")
            .arg("set")
            .arg(device_name)
            .arg("down")
            .spawn();

        match cmd_device_down {
            Ok(mut child) => {
                match child.wait() {
                    Ok(res) => {
                        if res.success() {
                            info!("Device [{}] is now down.", device_name);
                        } else {
                            error!("Could not temporarily disable interface [{}]. Check output.", device_name);
                            return;
                        }
                    },
                    Err(e) => {
                        error!("Could not temporarily disable interface [{}]: {}", device_name, e);
                        return;
                    }
                }
            }
            Err(e) => {
                error!("Could not temporarily disable interface [{}]: {}", device_name, e);
                return;
            }
        }

        info!("Enabling monitor mode on interface [{}] ...", device_name);

        let cmd_set_monitor = Command::new(cmd_iw)
            .arg("dev")
            .arg(device_name)
            .arg("set")
            .arg("monitor")
            .arg("control")
            .arg("otherbss")
            .spawn();

        match cmd_set_monitor {
            Ok(mut child) => {
                match child.wait() {
                    Ok(res) => {
                        if res.success() {
                            info!("Device [{}] is now in monitor mode.", device_name);
                        } else {
                            error!("Could not set interface [{}] monitor mode. Check output.", device_name);
                            return;
                        }
                    },
                    Err(e) => {
                        error!("Could not set interface [{}] monitor mode: {}", device_name, e);
                        return;
                    }
                }
            }
            Err(e) => {
                error!("Could not set interface [{}] monitor mode: {}", device_name, e);
                return;
            }
        }

        info!("Enabling interface [{}] ...", device_name);

        let cmd_device_up = Command::new(cmd_ip)
            .arg("link")
            .arg("set")
            .arg(device_name)
            .arg("up")
            .spawn();

        match cmd_device_up {
            Ok(mut child) => {
                match child.wait() {
                    Ok(res) => {
                        if res.success() {
                            info!("Device [{}] is now up.", device_name);
                        } else {
                            error!("Could not enable interface [{}]. Check output.", device_name);
                            return;
                        }
                    },
                    Err(e) => {
                        error!("Could not enable interface [{}]: {}", device_name, e);
                        return;
                    }
                }
            }
            Err(e) => {
                error!("Could not enable interface [{}]: {}", device_name, e);
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

            if frame.len() < 4{ 
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