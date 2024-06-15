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
    pub modalias: Option<String>,
    pub tx_power: Option<i16>,
    pub manufacturer_data: Option<Vec<u8>>,

    
    pub uuids: Option<Vec<String>>,
    pub service_data: Option<Vec<String>>,
    
    pub device: String,
    pub timestamp: DateTime<Utc>
}