use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use std::thread;
use anyhow::{anyhow, bail, Error};
use chrono::{Duration, Utc};
use clokwerk::{Scheduler, TimeUnits};
use log::{debug, error, info};
use strum_macros::Display;
use crate::configuration::WiFiEngagementInterfaceConfiguration;
use crate::configuration::EngagementTarget::UAV;
use crate::messagebus::bus::Bus;
use crate::metrics;
use crate::metrics::Metrics;
use crate::wireless::dot11::engagement::dot11_engagement_capture::Dot11EngagementCapture;
use crate::wireless::dot11::engagement::dot11_engagement_interface::Dot11EngagementInterface;
use crate::wireless::dot11::engagement::engagement_control::EngagementInterfaceStatus::{Idle, Seeking, Engaging};
use crate::wireless::dot11::engagement::uav_engagement_request::UavEngagementRequest;

pub struct EngagementControl {
    dot11_interfaces: HashMap<String, Arc<Dot11EngagementInterface>>,
    metrics: Arc<Mutex<Metrics>>,
    bus: Arc<Bus>
}

#[derive(Display, Copy, Debug, Clone, Eq, PartialEq)]
pub enum EngagementInterfaceStatus {
    Idle,
    Seeking,
    Engaging
}

const ENGAGEMENT_TIMEOUT_SECS: i64 = 60;
const SEEKER_TIMEOUT_SECS: i64 = 300;
const CONTROL_LOOP_INTERVAL_SECS: i64 = 10;

impl EngagementControl {

    pub fn new(dot11_interfaces_configuration: HashMap<String, WiFiEngagementInterfaceConfiguration>,
               metrics: Arc<Mutex<Metrics>>,
               bus: Arc<Bus>)
         -> Self {
        let mut dot11_interfaces = HashMap::new();

        for (interface_name, c) in dot11_interfaces_configuration {
            if !c.active {
                continue
            }

            dot11_interfaces.insert(
                interface_name.clone(),
                Arc::new(Dot11EngagementInterface::from_config(interface_name, c))
            );
        };

        Self { dot11_interfaces, metrics, bus }
    }

    pub fn initialize(&self) {
        info!("Initializing engagement control system.");

        // Start captures and associated monitor threads.
        for interface in self.dot11_interfaces.values() {
            // Interface engagement controller.
            let controller_interface = interface.clone();
            let controller_metrics = self.metrics.clone();
            let mut scheduler = Scheduler::new();
            scheduler.every((CONTROL_LOOP_INTERVAL_SECS as u32).seconds()).run(move || {
                Self::interface_control(controller_metrics.clone(), &controller_interface);
            });

            thread::spawn(move || {
                loop {
                    scheduler.run_pending();
                    thread::sleep(std::time::Duration::from_millis(500));
                }
            });

            // Capture.
            let captureinterface = interface.clone();
            let capturemetrics = self.metrics.clone();
            let capturebus = self.bus.clone();
            thread::spawn(move || {
                let capture = Arc::new(Dot11EngagementCapture::new(
                    captureinterface.clone(), capturemetrics.clone(), capturebus
                ));

                match capturemetrics.lock() {
                    Ok(mut metrics) => metrics.register_new_capture(
                        &captureinterface.name, metrics::CaptureType::WiFiEngagement),
                    Err(e) => error!("Could not acquire mutex of metrics: {}", e)
                }

                // Set capture ref.
                match captureinterface.capture.lock() {
                    Ok(mut c) => *c = Some(capture.clone()),
                    Err(e) => {
                        error!("Could not acquire capture lock on [{}]: {}",
                            captureinterface.name, e)
                    }
                };

                capture.run();
            });
        }
    }

    pub fn engage_uav(&self, request: UavEngagementRequest) -> Result<(), Error> {
        // Check if any interface is already engaging this UAV and skip request.
        for interface in self.dot11_interfaces.values() {
            match interface.capture.lock() {
                Ok(c) => {
                    if let Some(capture) = c.as_ref() {
                        match capture.current_target.lock() {
                            Ok(target) => {
                                if let Some(target) = target.as_ref() {
                                    if target.uav_id == request.uav_id {
                                        debug!("Ignoring request to engage UAV [{}] because \
                                        interface [{}] is already engaging it.",
                                            request.uav_id, interface.name);
                                        return Ok(());
                                    }
                                }
                            }
                            Err(e) => bail!("Could not acquire capture target lock on [{}]: {}",
                                interface.name, e)
                        }
                    }
                },
                Err(e) => bail!("Could not acquire capture lock on [{}]: {}", interface.name, e)
            }
        }

        // Find interfaces that can engage UAV.
        let interfaces: Vec<&str> = self.dot11_interfaces
            .iter()
            .filter(|(_, interface)| interface.engage.contains(&UAV))
            .map(|(name, _)| name.as_str())
            .collect();

        if interfaces.is_empty() {
            // This can be silently ignored because it simply means that we are not tracking UAVs.
            debug!("No engagement interface for requested type configured.");
            return Ok(());
        }

        // Find an idle interface.
        let selected_interface = interfaces
            .iter()
            .find(|name| self.dot11_interfaces.get(**name)
                .is_some_and(|i| i.status == Idle))
            .copied();

        let interface_name = selected_interface.ok_or_else(||
            anyhow!("All available engagement interfaces are busy."))?;

        Self::engagement_log(
            self.metrics.clone(),
            format!("Tasking [{}] to engage UAV [{}].", interface_name, request.uav_id)
        );

        let interface = self.dot11_interfaces
            .get(interface_name)
            .ok_or_else(|| anyhow!("Interface [{}] not in interface table.", interface_name))?;

        let mut guard = interface.capture
            .lock()
            .map_err(|e| anyhow!("Could not acquire interface [{}] capture: {}",
                interface_name, e))?;

        let capture = guard
            .as_mut()
            .ok_or_else(|| anyhow!("Interface [{}] is not fully initialized.",
                interface_name))?;

        capture.engage_uav_target(&request)
            .map_err(|e| anyhow!("Could not engage UAV target [{}] on [{}]: {}",
                request.mac_address, interface_name, e))?;

        Ok(())
    }

    fn interface_control(metrics: Arc<Mutex<Metrics>>, interface: &Arc<Dot11EngagementInterface>) {
        match interface.capture.lock() {
            Ok(capture) => {
                // Check if this interface has a capture (yet).
                if let Some(capture) = capture.as_ref() {
                    // Get capture status.
                    let capture_status = match capture.status.lock() {
                        Ok(status) => status.clone(),
                        Err(e) => {
                            error!("Could not acquire capture status lock on \
                                interface [{}]: {}", interface.name, e);
                            return;
                        }
                    };

                    /*
                     * Check when this capture saw the target the last time and if we need to
                     * configure it to SEEKING mode because it lost track or disengage the target
                     * entirely.
                     */
                    match capture.last_tracked_frame_timestamp.lock() {
                        Ok(ts) => {
                            if let Some(timestamp) = ts.as_ref() {
                                let secs_ago = Utc::now() - timestamp;

                                match capture_status {
                                    Idle => {
                                        // Capture is idle. Nothing to do.
                                    },
                                    Engaging => {
                                        // Check if we lost track of the target.
                                        if secs_ago > Duration::try_seconds(ENGAGEMENT_TIMEOUT_SECS)
                                                .unwrap() {
                                            Self::engagement_log(
                                                metrics,
                                                 format!("Interface [{}] lost engaged \
                                                    target. Tasking to start seeking.",
                                                         interface.name)
                                            );

                                            if capture.seek_current_target().is_err() {
                                                error!("Could not start seeking current target on \
                                                    [{}].", interface.name);
                                            }
                                        }
                                    },
                                    Seeking => {
                                        // Check if seeking did not detect the target.
                                        if secs_ago > Duration::try_seconds(SEEKER_TIMEOUT_SECS)
                                                .unwrap() {
                                            Self::engagement_log(
                                                metrics,
                                                format!("Interface [{}] timed out seeking and \
                                                    lost target. Requesting to \
                                                    disengage.", interface.name)
                                            );

                                            if capture.disengage_current_target().is_err() {
                                                error!("Could not disengage current target on \
                                                    [{}].", interface.name);
                                            }
                                        } else if secs_ago <= Duration
                                            ::try_seconds(CONTROL_LOOP_INTERVAL_SECS+2)
                                            .unwrap() {

                                            // Target re-acquired. Engage.
                                            Self::engagement_log(
                                                metrics,
                                                format!("Target re-acquired in seeking mode. \
                                                Tasking  [{}] to engage.", interface.name)
                                            );

                                            if capture.reengage_current_target().is_err() {
                                                error!("Could not re-engage current target on \
                                                [{}].", interface.name);
                                            }
                                        }
                                    },
                                }
                            }
                        },
                        Err(e) => {
                            error!("Could not acquire capture frame timestamp lock on \
                                interface [{}]: {}", interface.name, e);
                        }
                    }
                }
            },
            Err(e) => error!("Could not acquire capture lock on interface [{}]: {}",
                interface.name, e)
        }
    }

    pub fn engagement_log(metrics: Arc<Mutex<Metrics>>, log: String) {
        info!("{}", log);

        match metrics.lock() {
            Ok(mut metrics) => metrics.record_engagement_log(log),
            Err(e) => error!("Could not acquire mutex of metrics`: {}", e)
        }
    }
}