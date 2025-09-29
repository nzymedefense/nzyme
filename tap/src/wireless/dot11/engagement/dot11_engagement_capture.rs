use std::sync::{Arc, Mutex};
use std::thread;
use std::thread::sleep;
use std::time::Duration;
use anyhow::{bail, Error};
use chrono::{DateTime, Utc};
use crossbeam_channel::{bounded, tick, Receiver, Sender};
use log::{debug, error, info};
use pcap::{ Capture, Error as PcapErr, Linktype};
use crate::helpers::network::{dot11_channel_to_frequency, Nl80211Band};
use crate::messagebus::bus::Bus;
use crate::metrics::Metrics;
use crate::protocols::parsers::dot11::dot11_header_parser;
use crate::wireless::dot11::capture_helpers::prepare_device;
use crate::wireless::dot11::engagement::dot11_engagement_interface::Dot11EngagementInterface;
use crate::wireless::dot11::engagement::engagement_control::{EngagementControl, EngagementInterfaceStatus};
use crate::wireless::dot11::engagement::engagement_control::EngagementInterfaceStatus::{Engaging, Seeking, Idle};
use crate::wireless::dot11::engagement::uav_engagement_request::UavEngagementRequest;
use crate::wireless::dot11::frames::Dot11CaptureSource::Engagement;
use crate::wireless::dot11::frames::Dot11RawFrame;
use crate::wireless::dot11::nl::Nl;
use crate::wireless::dot11::supported_frequency::SupportedChannelWidth;

pub struct Dot11EngagementCapture {
    pub parent_interface: Arc<Dot11EngagementInterface>,
    pub current_target: Mutex<Option<UavEngagementRequest>>,
    pub status: Arc<Mutex<EngagementInterfaceStatus>>,
    pub last_tracked_frame_timestamp: Mutex<Option<DateTime<Utc>>>,
    pub last_contact_frequency: Mutex<Option<u16>>,
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
    pub fn new(parent_interface: Arc<Dot11EngagementInterface>,
               metrics: Arc<Mutex<Metrics>>,
               bus: Arc<Bus>) -> Self {
        let (cmd_tx, cmd_rx) = bounded(8192);

        Dot11EngagementCapture {
            parent_interface,
            metrics,
            bus,
            current_target: Mutex::new(None),
            status: Arc::new(Mutex::new(Idle)),
            last_tracked_frame_timestamp: Mutex::new(None),
            last_contact_frequency: Mutex::new(None),
            cmd_tx,
            cmd_rx
        }
    }

    pub fn run(&self) {
        info!("Starting WiFi engagement capture on [{}]", self.parent_interface.name);

        if let Err(e) = prepare_device(&self.parent_interface.name) {
            error!("Could not prepare device [{}]: {}", self.parent_interface.name, e);
            return;
        }

        // Start seeker.
        let parent_interface = self.parent_interface.clone();
        let status = self.status.clone();
        thread::spawn(move || {
            if parent_interface.supported_channels_2g.is_empty()
                && parent_interface.supported_channels_5g.is_empty()
                && parent_interface.supported_channels_6g.is_empty() {

                return;
            }

            let mut nl = match Nl::new() {
                Ok(nl) => nl,
                Err(e) => {
                    error!("Could not establish Netlink connection: {}", e);
                    return;
                }
            };

            let mut frequencies: Vec<u32> = Vec::new();
            for channel in &parent_interface.supported_channels_2g {
                let frequency: u16 = match dot11_channel_to_frequency(*channel, Nl80211Band::Band2GHz) {
                    Ok(frequency) => frequency,
                    Err(e) => {
                        error!("Could not get frequency for 2G channel <{}> of device [{}]: {}",
                            channel, parent_interface.name, e);
                        return;
                    }
                };

                frequencies.push(frequency as u32);
            }

            for channel in &parent_interface.supported_channels_5g {
                let frequency: u16 = match dot11_channel_to_frequency(*channel, Nl80211Band::Band5GHz) {
                    Ok(frequency) => frequency,
                    Err(e) => {
                        error!("Could not get frequency for 5G channel <{}> of device [{}]: {}",
                            channel, parent_interface.name, e);
                        return;
                    }
                };

                frequencies.push(frequency as u32);
            }

            for channel in &parent_interface.supported_channels_6g {
                let frequency: u16 = match dot11_channel_to_frequency(*channel, Nl80211Band::Band6GHz) {
                    Ok(frequency) => frequency,
                    Err(e) => {
                        error!("Could not get frequency for 6G channel <{}> of device [{}]: {}",
                            channel, parent_interface.name, e);
                        return;
                    }
                };

                frequencies.push(frequency as u32);
            }

            loop {
                let current_status = match status.lock() {
                    Ok(g) => *g,
                    Err(e) => {
                        error!("Could not acquire status lock: {}", e);
                        sleep(Duration::from_secs(1));
                        continue;
                    }
                };

                // Do nothing if not seeking.
                if current_status != Seeking {
                    sleep(Duration::from_secs(1));
                    continue;
                }

                for frequency in &*frequencies {
                    // We need a truly current status for each iteration.
                    let current_status = match status.lock() {
                        Ok(g) => *g,
                        Err(e) => {
                            error!("Could not acquire status lock: {}", e);
                            sleep(Duration::from_secs(1));
                            continue;
                        }
                    };

                    if current_status != Seeking {
                        /*
                         * Abort immediately if we are no longer seeking. This avoids setting a
                         * wrong frequency that we'll never successfully engage on.
                         */
                        break;
                    }

                    if let Err(e) = nl.set_device_frequency(
                        &parent_interface.name, *frequency, &SupportedChannelWidth::Mhz20) {

                        error!("Could not set seek frequency of engagement capture on [{}]: {}",
                            parent_interface.name, e)
                    }

                    debug!("Engagement capture interface [{}] seeking frequency set to [{} MHz / {}]",
                        parent_interface.name, frequency, &SupportedChannelWidth::Mhz20);
                    sleep(Duration::from_millis(250));
                }
            }
        });

        let device = Capture::from_device(self.parent_interface.name.as_ref())
            .unwrap()
            .promisc(true)
            .immediate_mode(true);

        let mut handle = match device.open() {
            Ok(h) => h,
            Err(e) => {
                error!("Could not get PCAP capture handle on [{}]: {}",
                self.parent_interface.name, e);
                return;
            }
        };

        if let Err(e) = handle.set_datalink(Linktype::IEEE802_11_RADIOTAP) {
            error!("Could not set datalink type on [{}]: {}", self.parent_interface.name, e);
            return;
        }

        handle = handle.setnonblock()
            .map_err(|e| {
                error!("Could not set PCAP capture handle to non-blocking on [{}]: {}",
                self.parent_interface.name, e);
                e
            })
            .ok()
            .unwrap();

        if let Err(e) = handle.filter("less 0", true) {
            error!("Could not set filter on [{}]: {}", self.parent_interface.name, e);
            return;
        }

        let stats = handle.stats();

        let t = tick(Duration::from_millis(100));

        'run: loop {
            crossbeam_channel::select! {
                recv(self.cmd_rx) -> msg => match msg {
                    Ok(Cmd::SetFilter(filter)) => {
                        match handle.filter(&filter, true) {
                            Ok(_) => debug!("Updated filter on [{}] to: {}",
                                self.parent_interface.name, filter),
                            Err(e) => error!("Failed to set filter on [{}]: {}",
                                self.parent_interface.name, e),
                        }
                    }

                    Ok(Cmd::Stop) | Err(_) => {
                        EngagementControl::engagement_log(
                            self.metrics.clone(),
                            format!("Stopping capture on [{}]", self.parent_interface.name)
                        );
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
                                    EngagementControl::engagement_log(
                                        self.metrics.clone(),
                                        format!("Initial lock on engagement capture [{}].",
                                            self.parent_interface.name)
                                    );
                                }

                                // Set new timestamp.
                                *timestamp = Some(Utc::now());
                            },
                            Err(e) => error!("Could not acquire engagement capture \
                                timestamp: {}", e)
                        }

                        let length = frame.data.len();

                        match self.metrics.lock() {
                            Ok(mut metrics) => {
                                match stats {
                                    Ok(stats) => {
                                        metrics.increment_processed_bytes_total(length as u32);
                                        metrics.update_capture(
                                            &self.parent_interface.name,
                                            true,
                                            stats.dropped,
                                            stats.if_dropped
                                        );
                                    },
                                    Err(ref e) => { // TOOD add error
                                        error!("Could not fetch handle stats for capture \
                                            [{}] metrics update: {}", self.parent_interface.name, e);
                                    }
                                }
                            },
                            Err(e) => error!("Could not acquire metrics mutex: {}", e)
                        }

                        if length < 4 {
                            debug!("Packet too small. Wouldn't even fit radiotap length \
                                information. Skipping.");
                            continue;
                        }

                        let data = Dot11RawFrame {
                            capture_source: Engagement,
                            interface_name: self.parent_interface.name.clone(),
                            data: frame.data.to_vec()
                        };

                        // Figure out what frequency this came from.
                        match self.last_contact_frequency.lock() {
                            Ok(mut frequency) => {
                                // Parse data to get radiotap header and frequency.
                                let packet_freq = match dot11_header_parser
                                    ::parse(&Arc::new(data.clone())) {

                                    Ok((header, _)) => {
                                        match header.header.frequency {
                                            Some(frequency) => frequency,
                                            None => {
                                                debug!("802.11 header is missing frequency.");
                                                continue;
                                            }
                                        }
                                    },
                                    Err(e) => {
                                        debug!("Could not parse 802.11 header: {}", e);
                                        continue;
                                    }
                                };

                                // Set new timestamp.
                                *frequency = Some(packet_freq);
                            },
                            Err(e) => error!("Could not acquire engagement capture \
                                timestamp: {}", e)
                        }

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
                        self.parent_interface.name, status)
                }
            },
            Err(e) => bail!("Could not acquire status mutex on capture device [{}]: {}",
                self.parent_interface.name, e),
        }

        // Set to initial frequency.
        let mut nl = match Nl::new() {
            Ok(nl) => nl,
            Err(e) => {
                bail!("Could not establish Netlink connection: {}", e);
            }
        };

        if let Err(e) = nl.set_device_frequency(&self.parent_interface.name,
                                                target.initial_frequency as u32,
                                                &target.initial_channel_width) {
            bail!("Could not tune engagement interface [{}] to frequency [{} Mhz / {}]: {}",
                self.parent_interface.name,
                target.initial_frequency,
                target.initial_channel_width,
                e
            )
        }

        debug!("Engagement capture [{}] now at frequency [{} Mhz / {}]",
            self.parent_interface.name,
            target.initial_frequency,
            target.initial_channel_width);

        let filter = format!("(wlan type mgt subtype beacon or wlan type mgt subtype probe-resp) \
            and (wlan addr2 {})", target.mac_address);

        debug!("Setting new engagement capture [{}] filter: {}",
            self.parent_interface.name, filter);

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

        EngagementControl::engagement_log(
            self.metrics.clone(),
            format!("Engaging UAV [{}] on [{} / {} Mhz / {}].",
                    target.uav_id,
                    self.parent_interface.name,
                    target.initial_frequency,
                    target.initial_channel_width)
        );

        Ok(())
    }

    pub fn seek_current_target(&self) -> Result<(), Error> {
        let target = match self.current_target.lock() {
            Ok(current_target) => {
                match current_target.as_ref() {
                    Some(target) => target.clone(),
                    None => bail!("Engagement interface [{}] has no current target.",
                        self.parent_interface.name)
                }
            },
            Err(e) => bail!("Could not acquire current target lock: {}", e)
        };

        // Update status to start seeking.
        match self.status.lock() {
            Ok(mut current_status) => *current_status = Seeking,
            Err(e) => bail!("Could not acquire status lock: {}", e)
        }

        EngagementControl::engagement_log(
            self.metrics.clone(),
            format!("Now seeking UAV [{}] on [{}].", target.uav_id, self.parent_interface.name)
        );

        Ok(())
    }

    pub fn reengage_current_target(&self) -> Result<(), Error> {
        let mut nl = match Nl::new() {
            Ok(nl) => nl,
            Err(e) => {
                bail!("Could not establish Netlink connection: {}", e);
            }
        };

        let last_contact_frequency = match self.last_contact_frequency.lock() {
            Ok(last_freq) => {
                match last_freq.as_ref() {
                    Some(freq) => *freq,
                    None => bail!("Engagement interface [{}] has no last contact frequency.",
                        self.parent_interface.name)
                }
            },
            Err(e) => bail!("Could not acquire current target lock: {}", e)
        };

        let target = match self.current_target.lock() {
            Ok(current_target) => {
                match current_target.as_ref() {
                    Some(target) => target.clone(),
                    None => bail!("Engagement interface [{}] has no current target.",
                        self.parent_interface.name)
                }
            },
            Err(e) => bail!("Could not acquire current target lock: {}", e)
        };

        // Update status to start seeking.
        match self.status.lock() {
            Ok(mut current_status) => *current_status = Engaging,
            Err(e) => bail!("Could not acquire status lock: {}", e)
        };

        if let Err(e) = nl.set_device_frequency(&self.parent_interface.name,
                                                last_contact_frequency as u32,
                                                &target.initial_channel_width) {
            bail!("Could not tune engagement interface [{}] to frequency [{} Mhz / {}]: {}",
                self.parent_interface.name,
                last_contact_frequency,
                target.initial_channel_width,
                e
            )
        }

        EngagementControl::engagement_log(
            self.metrics.clone(),
            format!("Re-engaged target [{}] on [{}].",
                    target.uav_id, self.parent_interface.name)
        );

        debug!("Engagement capture [{}] now at frequency [{} Mhz / {}]",
            self.parent_interface.name,
            target.initial_frequency,
            target.initial_channel_width);

        Ok(())
    }

    pub fn disengage_current_target(&self) -> Result<(), Error> {
        let target_id = match self.current_target.lock() {
            Ok(current_target) => {
                match current_target.as_ref() {
                    Some(target) => target.clone().uav_id,
                    None => bail!("Engagement interface [{}] has no current target.",
                        self.parent_interface.name)
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

        EngagementControl::engagement_log(
            self.metrics.clone(), format!("Disengaged [{target_id}].")
        );

        Ok(())
    }

}