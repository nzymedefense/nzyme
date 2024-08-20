use std::collections::HashMap;
use chrono::{DateTime, Utc};
use crate::bluetooth::detection::device_tagger::TagValue;

#[derive(Debug, Clone)]
pub struct BluetoothDevice {
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
    pub transport: String,
    pub tags: Option<HashMap<String, HashMap<String, TagValue>>>,
    pub last_seen: DateTime<Utc>
}