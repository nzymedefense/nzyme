use std::sync::{Arc, Mutex};
use std::thread;
use std::thread::sleep;
use std::time::Duration;
use anyhow::{anyhow, bail, Error};
use chrono::{DateTime, Utc};
use crossbeam_channel::{tick, Receiver, Sender};
use log::{debug, error, info, trace, warn};
use serialport::ClearBuffer;
use crate::messagebus::bus::Bus;
use crate::metrics::Metrics;
use crate::usb::usb::find_first_nzyme_usb_device_with_pid_and_serial;
use crate::wireless::dot11::engagement::engagement_capture::EngagementCapture;
use crate::wireless::dot11::engagement::engagement_control::{EngagementControl, EngagementInterfaceStatus};
use crate::wireless::dot11::engagement::engagement_control::EngagementInterfaceStatus::{Engaging, Idle, Seeking};
use crate::wireless::dot11::engagement::engagement_interface::EngagementInterface;
use crate::wireless::dot11::engagement::frequencies::extract_frequencies_from_interface;
use crate::wireless::dot11::engagement::uav_engagement_request::UavEngagementRequest;
use crate::wireless::dot11::sona::command_router::SonaCommandRouter;
use crate::wireless::dot11::sona::commands::{AddressedSonaCommand, SonaCommand};
use crate::wireless::dot11::sona::protocol::{parse_mac, process_decoded, send_set_filter, send_set_frequency, SonaFilter, MSG_TYPE_DOT11};
use crate::wireless::dot11::sona::sona::SONA_1_PID;
use crate::wireless::dot11::sona::sona_framer::{SonaFramer, SonaFramerError};
use crate::wireless::dot11::sona::sona_tools::extract_serial_from_interface_name;
use crate::wireless::dot11::sona::uptime_offset::UptimeOffset;
use crate::wireless::dot11::supported_frequency::SupportedChannelWidth;

const MAX_ACCUMULATED_BYTES_WITHOUT_DELIMITER: usize = 8192;
const RECONNECT_BACKOFF: Duration = Duration::from_secs(2);

pub struct SonaEngagementCapture {
    parent_interface: Arc<EngagementInterface>,
    device_serial: String,
    current_target: Mutex<Option<UavEngagementRequest>>,
    status: Arc<Mutex<EngagementInterfaceStatus>>,
    last_tracked_frame_timestamp: Mutex<Option<DateTime<Utc>>>,
    last_contact_frequency: Mutex<Option<u16>>,
    metrics: Arc<Mutex<Metrics>>,
    bus: Arc<Bus>,
    sona_command_router: Arc<SonaCommandRouter>,
    sona_command_router_receiver: Receiver<SonaCommand>
}

impl SonaEngagementCapture {
    pub fn new(parent_interface: Arc<EngagementInterface>,
               metrics: Arc<Mutex<Metrics>>,
               bus: Arc<Bus>) -> Self {
        let device_serial = extract_serial_from_interface_name("sona", &parent_interface.name)
            .expect("Failed to extract serial from interface");

        let mut sona_command_router = SonaCommandRouter::new();
        let sona_command_router_receiver = sona_command_router.register_capture(&device_serial);

        SonaEngagementCapture {
            parent_interface,
            device_serial,
            metrics,
            bus,
            current_target: Mutex::new(None),
            status: Arc::new(Mutex::new(Idle)),
            last_tracked_frame_timestamp: Mutex::new(None),
            last_contact_frequency: Mutex::new(None),
            sona_command_router: Arc::new(sona_command_router),
            sona_command_router_receiver
        }
    }

    fn connect(&self) -> Result<Box<dyn serialport::SerialPort>, Error> {
        let sona = find_first_nzyme_usb_device_with_pid_and_serial(SONA_1_PID, &self.device_serial)
            .map_err(|e| anyhow!("USB search failed: {}", e))?
            .ok_or_else(|| anyhow!("No Sona with serial [{}] found", &self.device_serial))?;

        let acm_port = sona.acm_port
            .ok_or_else(|| anyhow!("Sona [{}] at [{}:{}] has no ACM port",
                &self.device_serial, sona.bus, sona.address))?;

        debug!("Found Sona [{}] at [{}:{}]: {}",
            &self.device_serial, sona.bus, sona.address, acm_port);

        let mut port = serialport::new(&acm_port, 115_200)
            .timeout(Duration::from_millis(50))
            .open()
            .map_err(|e| anyhow!("Could not open ACM port [{}]: {}", acm_port, e))?;

        port.write_data_terminal_ready(true)
            .map_err(|e| anyhow!("Could not set DTR on [{}]: {}", acm_port, e))?;

        // Sync to the stream: settle and then drop any in-flight bytes.
        sleep(Duration::from_millis(150));
        if let Err(e) = port.clear(ClearBuffer::Input) {
            warn!("Could not flush input on Sona [{}]: {}", &self.device_serial, e);
        }

        info!("Connected to Sona [{}].", &self.device_serial);
        Ok(port)
    }

    fn drain_pending_commands(&self) {
        let mut count = 0usize;
        while self.sona_command_router_receiver.try_recv().is_ok() {
            count += 1;
        }
        if count > 0 {
            debug!("Drained {} stale commands for Sona [{}].", count, &self.device_serial);
        }
    }

    // Used during re-connect.
    fn restore_state(&self, port: &mut dyn serialport::SerialPort) -> Result<(), Error> {
        let status = self.current_status();
        info!("Restoring state on Sona [{}]: status={}", &self.device_serial, status);

        match status {
            Idle => {
                send_set_filter(port, &SonaFilter::DropAll)?;
            }
            Engaging | Seeking => {
                let target = self.current_target()
                    .ok_or_else(|| anyhow!("status={} but no current target", status))?;

                let mac = parse_mac(&target.mac_address)?;
                send_set_filter(port, &SonaFilter::MacMatch { mac })?;

                if status == Engaging {
                    let freq = self.last_contact_freq()
                        .unwrap_or(target.initial_frequency);
                    send_set_frequency(port, freq)?
                }
            }
        }
        Ok(())
    }

    fn last_contact_freq(&self) -> Option<u16> {
        self.last_contact_frequency.lock().ok().and_then(|g| *g)
    }

    fn capture_loop(&self, port: &mut dyn serialport::SerialPort) -> Result<(), Error> {
        let mut chunk = [0u8; 512];
        let mut framer = SonaFramer::new(MAX_ACCUMULATED_BYTES_WITHOUT_DELIMITER);
        let mut uptime_offset: Option<UptimeOffset> = None;

        let t = tick(Duration::from_millis(100));

        loop {
            /*
             * Drain pending host commands. Any write failure means the port
             * is dead (device disconnected?), bail to reconnect.
             */
            while let Ok(cmd) = self.sona_command_router_receiver.try_recv() {
                match cmd {
                    SonaCommand::SetFrequency(freq_mhz) => {
                        send_set_frequency(port, freq_mhz)?;
                        debug!("Sent SetFrequency({}) to Sona [{}]", freq_mhz, &self.device_serial);
                    }
                    SonaCommand::SetFilter(filter) => {
                        send_set_filter(port, &filter)?;
                        debug!("Sent filter to Sona [{}]: {:?}", &self.device_serial, filter);
                    }
                }
            }

            crossbeam_channel::select! {
                recv(t) -> _ => {
                    loop {
                        let available = port.bytes_to_read()
                            .map_err(|e| anyhow!("bytes_to_read: {}", e))?;
                        if available == 0 {
                            break;
                        }

                        match port.read(&mut chunk) {
                            Ok(n) if n > 0 => {
                                if let Err(SonaFramerError::Overflow) = framer.push(&chunk[..n]) {
                                    warn!("Accumulator overflow on Sona [{}], cleared.",
                                        &self.device_serial);
                                    continue;
                                }

                                framer.drain(|frame| match frame {
                                    Ok(decoded) => {
                                        if self.current_status() == Engaging
                                            && decoded.first() == Some(&MSG_TYPE_DOT11)
                                        {
                                            self.note_frame_arrival();
                                            process_decoded(
                                                &self.parent_interface.name,
                                                &decoded,
                                                &mut uptime_offset,
                                                self.metrics.clone(),
                                                self.bus.clone(),
                                            );
                                        }
                                    }
                                    Err(e) => trace!("COBS decode error: {}", e),
                                });
                            }
                            Ok(_) => break,
                            Err(ref e) if e.kind() == std::io::ErrorKind::TimedOut => break,
                            Err(e) => {
                                return Err(anyhow!("read error: {}", e));
                            }
                        }
                    }
                }
            }
        }
    }

    fn note_frame_arrival(&self) {
        if let Ok(mut ts) = self.last_tracked_frame_timestamp.lock() {
            if ts.is_none() {
                EngagementControl::engagement_log(
                    self.metrics.clone(),
                    format!("Initial lock on engagement capture [{}].",
                            self.parent_interface.name)
                );
            }
            *ts = Some(Utc::now());
        }
    }
}

impl EngagementCapture for SonaEngagementCapture {

    fn run(&self) {
        info!("Starting Sona engagement capture on [{}]", self.parent_interface.name);

        let router = self.sona_command_router.clone();
        std::thread::spawn(move || {
            router.run();
        });

        let parent_interface = self.parent_interface.clone();
        let status = self.status.clone();
        let tserial = self.device_serial.clone();
        let tsender = self.sona_command_router.sender.clone();
        thread::spawn(move || {
            if parent_interface.supported_channels_2g.is_empty()
                && parent_interface.supported_channels_5g.is_empty() {
                return;
            }

            let frequencies = extract_frequencies_from_interface(&parent_interface)
                .expect(&format!("Failed to gather frequencies of device [{}]",
                                 parent_interface.name));

            loop {
                if *status.lock().expect("Lock poisoned") != Seeking {
                    sleep(Duration::from_secs(1));
                    continue;
                }

                for frequency in &*frequencies {
                    if *status.lock().expect("Lock poisoned") != Seeking {
                        break;
                    }

                    set_frequency(&tsender, &tserial, &parent_interface.name, *frequency as u16);

                    debug!("Engagement capture interface [{}] seeking frequency set to [{} MHz / {}]",
                        parent_interface.name, frequency, &SupportedChannelWidth::Mhz20);
                    sleep(Duration::from_millis(1000));
                }
            }
        });

        /*
         * Reconnect loop. Anything that throws inside drops back here and
         * we try again after a backoff.
         */
        loop {
            let mut port_handle = match self.connect() {
                Ok(p) => p,
                Err(e) => {
                    warn!("Could not connect to Sona [{}]: {}. Retrying in {:?}.",
                        &self.device_serial, e, RECONNECT_BACKOFF);
                    sleep(RECONNECT_BACKOFF);
                    continue;
                }
            };

            self.drain_pending_commands();

            if let Err(e) = self.restore_state(&mut *port_handle) {
                warn!("Could not restore state on Sona [{}]: {}. Reconnecting.",
                    &self.device_serial, e);
                sleep(RECONNECT_BACKOFF);
                continue;
            }

            if let Err(e) = self.capture_loop(&mut *port_handle) {
                warn!("Sona [{}] capture loop ended: {}. Reconnecting.",
                    &self.device_serial, e);
                sleep(RECONNECT_BACKOFF);
                continue;
            }
        }
    }

    fn engage_uav_target(&self, target: &UavEngagementRequest) -> Result<(), Error> {
        let status = self.current_status();
        if status != Idle {
            bail!("Cannot engage new UAV target on [{}]. Interface is not Idle but [{}].",
                self.parent_interface.name, status)
        }

        set_frequency(
            &self.sona_command_router.sender,
            &self.device_serial,
            &self.parent_interface.name,
            target.initial_frequency
        );

        debug!("Engagement capture [{}] now at frequency [{} Mhz / {}]",
            self.parent_interface.name,
            target.initial_frequency,
            target.initial_channel_width);

        let filter = SonaFilter::MacMatch {
            mac: parse_mac(&target.mac_address)?,
        };

        debug!("Setting new engagement capture [{}] filter: {:?}", self.parent_interface.name, filter);
        set_filter(&self.sona_command_router.sender, &self.parent_interface.name,
                   &self.device_serial, filter);

        self.set_current_target(Some(target.clone()))?;
        self.set_status(Engaging)?;

        EngagementControl::engagement_log(
            self.metrics.clone(),
            format!("Engaging UAV [{}] on [{} / {} Mhz / {}].",
                    target.uav_id,
                    self.parent_interface.name,
                    target.initial_frequency,
                    target.initial_channel_width)
        );

        match self.last_tracked_frame_timestamp.lock() {
            Ok(mut timestamp) => {
                // Set first timestamp.
                *timestamp = Some(Utc::now());
            },
            Err(e) => error!("Could not acquire engagement capture timestamp: {}", e)
        }

        Ok(())
    }

    fn seek_current_target(&self) -> Result<(), Error> {
        let target = match self.current_target.lock() {
            Ok(current_target) => match current_target.as_ref() {
                Some(target) => target.clone(),
                None => bail!("Engagement interface [{}] has no current target.",
                    self.parent_interface.name)
            },
            Err(e) => bail!("Could not acquire current target lock: {}", e)
        };

        self.set_status(Seeking)?;

        EngagementControl::engagement_log(
            self.metrics.clone(),
            format!("Now seeking UAV [{}] on [{}].", target.uav_id, self.parent_interface.name)
        );

        Ok(())
    }

    fn reengage_current_target(&self) -> Result<(), Error> {
        let last_contact_frequency = match self.last_contact_frequency.lock() {
            Ok(last_freq) => match last_freq.as_ref() {
                Some(freq) => *freq,
                None => bail!("Engagement interface [{}] has no last contact frequency.",
                    self.parent_interface.name)
            },
            Err(e) => bail!("Could not acquire current target lock: {}", e)
        };

        let target = match self.current_target.lock() {
            Ok(current_target) => match current_target.as_ref() {
                Some(target) => target.clone(),
                None => bail!("Engagement interface [{}] has no current target.",
                    self.parent_interface.name)
            },
            Err(e) => bail!("Could not acquire current target lock: {}", e)
        };

        self.set_status(Engaging)?;

        set_frequency(&self.sona_command_router.sender,
                      &self.device_serial,
                      &self.parent_interface.name,
                      last_contact_frequency);

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

    fn disengage_current_target(&self) -> Result<(), Error> {
        let target_id = match self.current_target.lock() {
            Ok(current_target) => match current_target.as_ref() {
                Some(target) => target.clone().uav_id,
                None => bail!("Engagement interface [{}] has no current target.",
                    self.parent_interface.name)
            },
            Err(e) => bail!("Could not acquire current target lock: {}", e)
        };

        set_filter(&self.sona_command_router.sender,
                   &self.parent_interface.name,
                   &self.device_serial,
                   SonaFilter::DropAll);

        self.set_status(Idle)?;
        self.set_current_target(None)?;

        EngagementControl::engagement_log(
            self.metrics.clone(), format!("Disengaged [{target_id}].")
        );

        Ok(())
    }

    fn current_status(&self) -> EngagementInterfaceStatus {
        *self.status.lock().expect("Lock poisoned")
    }

    fn set_status(&self, new: EngagementInterfaceStatus) -> Result<(), Error> {
        let mut g = self.status.lock()
            .map_err(|e| anyhow::anyhow!("Lock poisoned: {e}"))?;
        *g = new;
        Ok(())
    }

    fn current_target(&self) -> Option<UavEngagementRequest> {
        self.current_target.lock().expect("Lock poisoned").clone()
    }

    fn set_current_target(&self, new: Option<UavEngagementRequest>) -> Result<(), Error> {
        let mut g = self.current_target.lock()
            .map_err(|e| anyhow::anyhow!("Lock poisoned: {e}"))?;
        *g = new;
        Ok(())
    }

    fn last_tracked_frame_timestamp(&self) -> Option<DateTime<Utc>> {
        *self.last_tracked_frame_timestamp.lock().expect("Lock poisoned")
    }
}

fn set_frequency(sender: &Sender<AddressedSonaCommand>, device_serial: &str, interface_name: &str, frequency: u16) {
    match sender.send(AddressedSonaCommand {
        sona_device_serial: device_serial.to_string(),
        cmd: SonaCommand::SetFrequency(frequency),
    }) {
        Ok(()) => trace!("Sent command to set frequency of Sona [{}] to <{}>", interface_name, frequency),
        Err(e) => error!("Could not send command to set frequency of Sona [{}] to <{}>: {}",
            interface_name, frequency, e)
    }
}

fn set_filter(sender: &Sender<AddressedSonaCommand>,
              interface_name: &str,
              device_serial: &str,
              filter: SonaFilter) {
    match sender.send(AddressedSonaCommand {
        sona_device_serial: device_serial.to_string(),
        cmd: SonaCommand::SetFilter(filter.clone()),
    }) {
        Ok(()) => debug!("Sent filter to Sona [{}]: {:?}", interface_name, filter),
        Err(e) => error!("Could not send filter to Sona [{}]: {:?}: {}",
            interface_name, filter, e),
    }
}