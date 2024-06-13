use chrono::{DateTime, Utc};

#[derive(Debug)]
pub struct BluetoothDeviceAdvertisement {
    pub mac: String,
    pub name: Option<String>,
    pub rssi: Option<i16>,
    pub company_id: Option<u16>,
    pub alias: String,
    pub class: Option<u32>,
    pub appearance: Option<u32>,
    pub legacy_pairing: Option<bool>,
    pub uuids: Option<Vec<String>>,
    pub modalias: Option<String>,
    pub manufacturer_data: Vec<u8>,
    pub service_data: Vec<String>,
    pub tx_power: Option<i16>,
    pub adapter: String,
    pub timestamp: DateTime<Utc>
}