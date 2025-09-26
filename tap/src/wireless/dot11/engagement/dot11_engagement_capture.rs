use std::sync::{Arc, Mutex};
use std::time::Duration;
use anyhow::{bail, Error};
use chrono::{DateTime, Utc};
use crossbeam_channel::{bounded, tick, Receiver, Sender};
use log::{debug, error, info};
use pcap::{ Capture, Error as PcapErr, Linktype};use crate::messagebus::bus::Bus;
use crate::metrics::Metrics;
use crate::wireless::dot11::capture_helpers::prepare_device;
use crate::wireless::dot11::engagement::engagement_control::EngagementInterfaceStatus;
use crate::wireless::dot11::engagement::engagement_control::EngagementInterfaceStatus::{Engaging, Seeking, Idle};
use crate::wireless::dot11::engagement::uav_engagement_request::UavEngagementRequest;
use crate::wireless::dot11::frames::Dot11CaptureSource::Engagement;
use crate::wireless::dot11::frames::Dot11RawFrame;
use crate::wireless::dot11::nl::Nl;

pub struct Dot11EngagementCapture {
    pub parent_interface_name: String,
    pub current_target: Mutex<Option<UavEngagementRequest>>,
    pub status: Mutex<EngagementInterfaceStatus>,
    pub last_tracked_frame_timestamp: Mutex<Option<DateTime<Utc>>>,
    pub metrics: Arc<Mutex<Metrics>>,
    pub bus: Arc<Bus>,
    cmd_tx: Sender<Cmd>,
    cmd_rx: Receiver<Cmd>,
}

enum Cmd {
    SetFilter(String),
    Stop,
}

impl Dot11EngagementCapture {
    pub fn new(parent_interface_name: String,
               metrics: Arc<Mutex<Metrics>>,
               bus: Arc<Bus>) -> Self {
        let (cmd_tx, cmd_rx) = bounded(8192);

        Dot11EngagementCapture {
            parent_interface_name,
            metrics,
            bus,
            current_target: Mutex::new(None),
            status: Mutex::new(Idle),
            last_tracked_frame_timestamp: Mutex::new(None),
            cmd_tx,
            cmd_rx
        }
    }

    pub fn run(&self) {
        info!("Starting WiFi engagement capture on [{}]", self.parent_interface_name);
        if let Err(e) = prepare_device(&self.parent_interface_name) {
            error!("Could not prepare device [{}]: {}", self.parent_interface_name, e);
            return;
        }

        let device = Capture::from_device(self.parent_interface_name.as_ref())
            .unwrap()
            .promisc(true)
            .immediate_mode(true);

        let mut handle = match device.open() {
            Ok(h) => h,
            Err(e) => {
                error!("Could not get PCAP capture handle on [{}]: {}",
                self.parent_interface_name, e);
                return;
            }
        };

        if let Err(e) = handle.set_datalink(Linktype::IEEE802_11_RADIOTAP) {
            error!("Could not set datalink type on [{}]: {}", self.parent_interface_name, e);
            return;
        }

        handle = handle.setnonblock()
            .map_err(|e| {
                error!("Could not set PCAP capture handle to non-blocking on [{}]: {}",
                self.parent_interface_name, e);
                e
            })
            .ok()
            .unwrap();

        if let Err(e) = handle.filter("less 0", true) {
            error!("Could not set filter on [{}]: {}", self.parent_interface_name, e);
            return;
        }

        let t = tick(Duration::from_millis(100));

        'run: loop {
            crossbeam_channel::select! {
                recv(self.cmd_rx) -> msg => match msg {
                    Ok(Cmd::SetFilter(filter)) => {
                        match handle.filter(&filter, true) {
                            Ok(_) => debug!("Updated filter on [{}] to: {}", self.parent_interface_name, filter),
                            Err(e) => error!("Failed to set filter on [{}]: {}", self.parent_interface_name, e),
                        }
                    }

                    Ok(Cmd::Stop) | Err(_) => {
                        info!("Stopping capture on [{}]", self.parent_interface_name);
                        break 'run;
                    }
                },

                recv(t) -> _ => {
                    // Drain all frames.
                    loop {
                        let frame = match handle.next_packet() {
                            Ok(packet) => packet,
                            Err(PcapErr::NoMorePackets) | Err(PcapErr::TimeoutExpired) => break,
                            Err(e) => {
                                error!("Engagement capture exception: {}", e);
                                continue;
                            }
                        };

                        match self.last_tracked_frame_timestamp.lock() {
                            Ok(mut timestamp) => {
                                if timestamp.is_none() {
                                    info!("Initial lock on engagement capture [{}].",
                                        self.parent_interface_name);
                                }

                                // Set new timestamp.
                                *timestamp = Some(Utc::now());
                            },
                            Err(e) => error!("Could not acquire engagement capture \
                                timestamp: {}", e)
                        }

                        let length = frame.data.len();

                        if length < 4 {
                            debug!("Packet too small. Wouldn't even fit radiotap length \
                                information. Skipping.");
                            continue;
                        }

                        let data = Dot11RawFrame {
                            capture_source: Engagement,
                            interface_name: self.parent_interface_name.clone(),
                            data: frame.data.to_vec()
                        };

                        // Write to Dot11 broker pipeline.
                        match self.bus.dot11_broker.sender.lock() {
                            Ok(mut sender) => { sender.send_packet(Arc::new(data), length as u32) },
                            Err(e) => error!("Could not acquire 802.11 handler broker mutex: {}", e)
                        }
                    }
                }
            }
        }
    }

    pub fn engage_uav_target(&self, target: &UavEngagementRequest) -> Result<(), Error> {
        match self.status.lock() {
            Ok(status) => {
                if *status != Idle {
                    bail!("Cannot engage new UAV target on [{}]. Interface is not Idle but [{}].",
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

        let filter = format!("(wlan type mgt subtype beacon or wlan type mgt subtype probe-resp) \
            and (wlan addr2 {})", target.mac_address);

        debug!("Setting new engagement capture [{}] filter: {}",
            self.parent_interface_name, filter);

        self.cmd_tx.send(Cmd::SetFilter(filter))
            .map_err(|e| anyhow::anyhow!("failed to send SetFilter command: {e}"))?;

        // Update status and current target.
        match self.current_target.lock() {
            Ok(mut current_target) => *current_target = Some(target.clone()),
            Err(e) => bail!("Could not acquire current target lock: {}", e)
        }
        match self.status.lock() {
            Ok(mut current_status) => *current_status = Engaging,
            Err(e) => bail!("Could not acquire status lock: {}", e)
        }

        info!("Engaging UAV [{}] on [{} / {} Mhz / {}].",
            target.uav_id,
            self.parent_interface_name,
            target.initial_frequency,
            target.initial_channel_width);

        Ok(())
    }

    pub fn seek_current_target(&self) -> Result<(), Error> {
        let target = match self.current_target.lock() {
            Ok(current_target) => {
                match current_target.as_ref() {
                    Some(target) => target.clone(),
                    None => bail!("Engagement interface [{}] has no current target.",
                        self.parent_interface_name)
                }
            },
            Err(e) => bail!("Could not acquire current target lock: {}", e)
        };

        // Update status.
        match self.status.lock() {
            Ok(mut current_status) => *current_status = Seeking,
            Err(e) => bail!("Could not acquire status lock: {}", e)
        }

        info!("Now seeking [{}] on [{}].", target.uav_id, self.parent_interface_name);

        Ok(())
    }

    pub fn disengage_current_target(&self) -> Result<(), Error> {
        let target_id = match self.current_target.lock() {
            Ok(current_target) => {
                match current_target.as_ref() {
                    Some(target) => target.clone().uav_id,
                    None => bail!("Engagement interface [{}] has no current target.",
                        self.parent_interface_name)
                }
            },
            Err(e) => bail!("Could not acquire current target lock: {}", e)
        };

        // Set filter to not match anything.
        self.cmd_tx.send(Cmd::SetFilter("less 0".to_string()))
            .map_err(|e| anyhow::anyhow!("failed to send SetFilter command: {e}"))?;

        // Update status.
        match self.status.lock() {
            Ok(mut current_status) => *current_status = Idle,
            Err(e) => bail!("Could not acquire status lock: {}", e)
        }

        // Reset current target.
        match self.current_target.lock() {
            Ok(mut current_target) => *current_target = None,
            Err(e) => bail!("Could not acquire current target lock: {}", e)
        }

        info!("Disengaged [{}].", target_id);


        Ok(())
    }

}