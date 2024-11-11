use std::collections::HashMap;
use std::sync::MutexGuard;
use base64::Engine;
use chrono::{DateTime, Utc};
use serde::{Serialize, Serializer};
use crate::wireless::bluetooth::detection::device_tagger::TagValue;
use crate::wireless::bluetooth::tables::bluetooth_device::BluetoothDevice;

#[derive(Serialize)]
pub struct BluetoothDevicesReport {
    pub devices: Vec<BluetoothDeviceReport>
}

#[derive(Serialize)]
pub struct BluetoothDeviceReport {
    pub mac: String,
    pub name: Option<String>,
    pub rssi: Option<i16>,
    pub company_id: Option<u16>,
    pub alias: String,
    pub class: Option<u32>,
    pub appearance: Option<u32>,
    pub modalias: Option<String>,
    pub tx_power: Option<i16>,
    pub manufacturer_data: Option<String>, // Base64
    pub uuids: Option<Vec<String>>,
    pub service_data: Option<Vec<String>>,
    pub device: String,
    pub transport: String,
    pub tags: Option<HashMap<String, HashMap<String, TagValue>>>,
    pub last_seen: DateTime<Utc>
}

impl Serialize for TagValue {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: Serializer,
    {
        match *self {
            TagValue::Byte(ref v) => serializer.serialize_u8(*v),
            TagValue::Text(ref v) => serializer.serialize_str(v),
            TagValue::Boolean(ref v) => serializer.serialize_bool(*v),
        }
    }
}

pub fn generate_report(d: &MutexGuard<HashMap<String, BluetoothDevice>>) -> BluetoothDevicesReport {
    let mut devices: Vec<BluetoothDeviceReport> = Vec::new();

    for device in d.values() {
        devices.push(BluetoothDeviceReport {
            mac: device.mac.clone(),
            name: device.name.clone(),
            rssi: device.rssi,
            company_id: device.company_id,
            alias: device.alias.clone(),
            class: device.class,
            appearance: device.appearance,
            modalias: device.modalias.clone(),
            tx_power: device.tx_power,
            manufacturer_data: device.manufacturer_data.clone().map(|m|
                base64::engine::general_purpose::STANDARD.encode(m)
            ),
            uuids: device.uuids.clone(),
            service_data: device.service_data.clone(),
            device: device.device.clone(),
            tags: device.tags.clone(),
            transport: device.transport.clone(),
            last_seen: device.last_seen,
        })
    }

    BluetoothDevicesReport { devices }
}