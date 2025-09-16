use std::collections::HashMap;
use log::info;
use crate::configuration::WiFiEngagementInterface;

pub struct EngagementControl {
    dot11_interfaces: HashMap<String, WiFiEngagementInterface>
}

impl EngagementControl {

    pub fn new(dot11_interfaces: HashMap<String, WiFiEngagementInterface>) -> Self {
        Self { dot11_interfaces }
    }

    pub fn initialize(&self) {
        info!("Initializing engagement control system.");
        info!("802.11 engagement control interfaces: {:?}", self.dot11_interfaces);
    }

}