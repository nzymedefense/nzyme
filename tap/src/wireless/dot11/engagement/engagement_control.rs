use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use std::thread;
use anyhow::{anyhow, bail, Error};
use log::{error, info};
use strum_macros::Display;
use crate::configuration::WiFiEngagementInterfaceConfiguration;
use crate::configuration::EngagementTarget::UAV;
use crate::messagebus::bus::Bus;
use crate::metrics::Metrics;
use crate::wireless::dot11::engagement::dot11_engagement_capture::Dot11EngagementCapture;
use crate::wireless::dot11::engagement::dot11_engagement_interface::Dot11EngagementInterface;
use crate::wireless::dot11::engagement::engagement_control::EngagementInterfaceStatus::Idle;
use crate::wireless::dot11::engagement::uav_engagement_request::UavEngagementRequest;

pub struct EngagementControl {
    dot11_interfaces: HashMap<String, Arc<Dot11EngagementInterface>>,
    metrics: Arc<Mutex<Metrics>>,
    bus: Arc<Bus>
}

#[derive(Display, Debug, Clone, Eq, PartialEq)]
pub enum EngagementInterfaceStatus {
    Idle,
    Seeking,
    Tracking
}

impl EngagementControl {

    pub fn new(dot11_interfaces_configuration: HashMap<String, WiFiEngagementInterfaceConfiguration>,
               metrics: Arc<Mutex<Metrics>>,
               bus: Arc<Bus>)
         -> Self {
        let mut dot11_interfaces = HashMap::new();

        for (interface_name, c) in dot11_interfaces_configuration {
            dot11_interfaces.insert(
                interface_name.clone(),
                Arc::new(Dot11EngagementInterface::from_config(interface_name, c))
            );
        };

        Self { dot11_interfaces, metrics, bus }
    }

    pub fn initialize(&self) {
        info!("Initializing engagement control system.");

        // Start captures.
        for (interface_name, interface) in self.dot11_interfaces.clone() {
            let captureinterface_name = interface_name.clone();
            let captureinterface = interface.clone();
            let capturemetrics = self.metrics.clone();
            let capturebus = self.bus.clone();

            thread::spawn(move || {
                let capture = Arc::new(Dot11EngagementCapture::new(
                    captureinterface_name.clone(), capturemetrics, capturebus
                ));

                // Set capture ref.
                match captureinterface.capture.lock() {
                    Ok(mut c) => *c = Some(capture.clone()),
                    Err(e) => {
                        error!("Could not acquire capture lock on [{}]: {}",
                            captureinterface_name, e)
                    }
                };

                capture.run();
            });
        }
    }

    pub fn engage_uav(&self, request: UavEngagementRequest) -> Result<(), Error> {
        let interfaces: Vec<&str> = self.dot11_interfaces
            .iter()
            .filter(|(_, interface)| interface.engage.contains(&UAV))
            .map(|(name, _)| name.as_str())
            .collect();

        if interfaces.is_empty() {
            bail!("No engagement interface for requested type configured.");
        }

        let selected_interface = interfaces
            .iter()
            .find(|name| self.dot11_interfaces.get(**name)
                .is_some_and(|i| i.status == Idle))
            .copied();

        let interface_name = selected_interface.ok_or_else(||
            anyhow!("All available engagement interfaces are busy."))?;

        info!("Engaging UAV [{}] with [{}].", request.uav_id, interface_name);

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

        capture.track_target(&request)
            .map_err(|e| anyhow!("Could not track target [{}] on [{}]: {}",
                request.mac_address, interface_name, e))?;

        Ok(())
    }

}