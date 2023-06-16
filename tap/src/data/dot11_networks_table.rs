use std::{sync::Mutex, collections::HashMap};

use chrono::{DateTime, Utc};

use crate::dot11::frames::{SecurityInformation, Dot11BeaconFrame};

pub struct Dot11NetworksTable {
    bssids: Mutex<HashMap<String, Bssid>>
}

pub struct Bssid {
    pub bssid: String,
    pub advertised_networks: HashMap<String, AdvertisedNetwork>,
    pub signal: SignalStrength,
    pub fingerprints: Vec<String>,
    pub updated_at: DateTime<Utc>
}
pub struct AdvertisedNetwork {
    pub bssid: String,
    pub ssid: String,
    pub security: Vec<SecurityInformation>,
    pub fingerprints: Vec<String>,
    pub wps: bool,
    pub signal: SignalStrength,
    pub updated_at: DateTime<Utc>
}

pub struct SignalStrength {
    pub min: u16,
    pub max: u16,
    pub average: f32
}

impl Dot11NetworksTable {

    pub fn new() -> Self {
        Self {
            bssids: Mutex::new(HashMap::new())
        }
    }

    pub fn register_beacon_frame(&mut self, beacon: Dot11BeaconFrame) {
        
    }

}