use std::sync::Arc;
use chrono::{DateTime, Utc};
use crate::bluetooth::bluetooth_device_advertisement::BluetoothDeviceAdvertisement;

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
    pub last_seen: DateTime<Utc>
}

impl From<BluetoothDeviceAdvertisement> for BluetoothDevice {
    fn from(v: BluetoothDeviceAdvertisement) -> Self {
        BluetoothDevice {
            mac: v.mac,
            name: v.name,
            rssi: v.rssi,
            company_id: v.company_id,
            alias: v.alias,
            class: v.class,
            appearance: v.appearance,
            modalias: v.modalias,
            tx_power: v.tx_power,
            manufacturer_data: v.manufacturer_data,
            uuids: v.uuids,
            service_data: v.service_data,
            device: v.device,
            transport: v.transport,
            last_seen: v.timestamp
        }
    }
}

impl From<Arc<BluetoothDeviceAdvertisement>> for BluetoothDevice {
    fn from(v: Arc<BluetoothDeviceAdvertisement>) -> Self {
        let advertisement: BluetoothDeviceAdvertisement = (*v).clone();
        advertisement.into()
    }
}