use std::sync::{Arc, Mutex};
use anyhow::{bail, Error};
use log::{debug, error, info};
use pcap::{Active, Capture};
use crate::messagebus::bus::Bus;
use crate::metrics::Metrics;
use crate::wireless::dot11::capture_helpers::prepare_device;
use crate::wireless::dot11::engagement::engagement_control::EngagementInterfaceStatus;
use crate::wireless::dot11::engagement::engagement_control::EngagementInterfaceStatus::Idle;
use crate::wireless::dot11::engagement::uav_engagement_request::UavEngagementRequest;
use crate::wireless::dot11::nl::Nl;

pub struct Dot11EngagementCapture {
    pub parent_interface_name: String,
    pub current_target: Arc<Mutex<Option<UavEngagementRequest>>>,
    pub status: Arc<Mutex<EngagementInterfaceStatus>>,
    pub handle: Arc<Mutex<Option<Capture<Active>>>>,
    pub metrics: Arc<Mutex<Metrics>>,
    pub bus: Arc<Bus>
}

impl Dot11EngagementCapture {

    pub fn new(parent_interface_name: String,
               metrics: Arc<Mutex<Metrics>>,
               bus: Arc<Bus>) -> Self {
        Dot11EngagementCapture {
            parent_interface_name,
            metrics,
            bus,
            current_target: Arc::new(Mutex::new(None)),
            status: Arc::new(Mutex::new(Idle)),
            handle: Arc::new(Mutex::new(None))
        }
    }

    pub fn run(&self) {
        info!("Starting WiFi engagement capture on [{}]", self.parent_interface_name);

        if let Err(e) = prepare_device(&self.parent_interface_name) {
            error!("Could not prepare device [{}]: {}", self.parent_interface_name, e);
            return;
        }

        let device = match pcap::Capture::from_device(self.parent_interface_name.as_ref()) {
            Ok(device) => {
                device
                    .immediate_mode(false)
                    .timeout(100)
                    .promisc(true)
            },
            Err(e) => {
                error!("Could not get PCAP device handle on [{}]: {}",
                    self.parent_interface_name, e);
                return;
            }
        };

        let mut handle = match device.open() {
            Ok(handle) => handle,
            Err(e) => {
                error!("Could not get PCAP capture handle on [{}]: {}",
                    self.parent_interface_name, e);
                return;
            }
        };

        if let Err(e) = handle.set_datalink(pcap::Linktype::IEEE802_11_RADIOTAP) {
            error!("Could not set datalink type on [{}]: {}",
                self.parent_interface_name, e);
            return;
        }

        // Start idle (match nothing)
        if let Err(e) = handle.filter("less 0", true) {
            error!("Could not set filter on [{}]: {}",
                self.parent_interface_name, e);
            return;
        }

        // Store handle.
        match self.handle.lock() {
            Ok(mut h) => *h = Some(handle),
            Err(e) => {
                error!("Could not acquire handle mutex of engagement interface [{}]: {}",
                    self.parent_interface_name, e);
                return;
            }
        };

        // XXX TODO: Don't share handle. Use single handle, send commands via channel, use select {}

        loop {
            match self.handle.clone().lock() {
                Ok(mut h) => {
                    match h.as_mut().unwrap().next_packet() {
                        Ok(packet) => {
                            info!("ENGAGEMENT FRAME {}", self.parent_interface_name);
                        },
                        Err(e) => {
                            error!("Capture exception: {}", e);
                            continue;
                        }
                    }
                },
                Err(e) => {
                    error!("Could not acquire handle mutex of engagement interface [{}]: {}",
                    self.parent_interface_name, e);
                    continue;
                }
            };
        }
    }

    pub fn track_target(&self, target: &UavEngagementRequest) -> Result<(), Error> {
        match self.status.lock() {
            Ok(status) => {
                if *status != Idle {
                    bail!("Cannot track new target on [{}]. Interface is not Idle but [{}].",
                        self.parent_interface_name, status)
                }
            },
            Err(e) => bail!("Could not acquire status mutex on capture device [{}]: {}",
                self.parent_interface_name, e),
        }


        // Set to initial frequency.
        let mut nl = match Nl::new() {
            Ok(nl) => nl,
            Err(e) => {
                bail!("Could not establish Netlink connection: {}", e);
            }
        };

        if let Err(e) = nl.set_device_frequency(&self.parent_interface_name,
                                                target.initial_frequency as u32,
                                                &target.initial_channel_width) {
            bail!("Could not tune engagement interface [{}] to frequency [{} Mhz / {}]: {}",
                self.parent_interface_name,
                target.initial_frequency,
                target.initial_channel_width,
                e
            )
        }

        debug!("Engagement capture [{}] now at frequency [{} Mhz / {}]",
            self.parent_interface_name,
            target.initial_frequency,
            target.initial_channel_width);

        // Set filter.
        match self.handle.clone().lock() {
            Ok(mut handle) => {
                if let Err(e) = handle.as_mut().unwrap().filter("", true) {
                    bail!("Could not set filter on [{}]: {}",self.parent_interface_name, e);
                };
            },
            Err(e) => {
                error!("Could not acquire handle mutex of engagement interface [{}]: {}",
                    self.parent_interface_name, e);
            }
        }

        debug!("New engagement capture [{}] filter: {}", self.parent_interface_name, "");

        info!("Tracking UAV [{}] on [{} / {} Mhz / {}].",
            target.uav_id,
            self.parent_interface_name,
            target.initial_frequency,
            target.initial_channel_width);

        Ok(())
    }

}