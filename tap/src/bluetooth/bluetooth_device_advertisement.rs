use chrono::{DateTime, Utc};
use crate::helpers::sizes;

#[derive(Debug, Clone)]
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
    pub transport: String,
    pub timestamp: DateTime<Utc>
}

impl BluetoothDeviceAdvertisement {

    pub fn estimate_struct_size(&self) -> u32 {
        let mut x: u32 = 12; // known size members

        x += self.mac.len() as u32;
        x += self.alias.len() as u32;
        x += self.device.len() as u32;
        x += self.transport.len() as u32;

        x += sizes::optional_size(&self.name, |x| x.len() as u32);
        x += sizes::optional_size(&self.rssi, |_| 2);
        x += sizes::optional_size(&self.company_id, |_| 2);
        x += sizes::optional_size(&self.class, |_| 4);
        x += sizes::optional_size(&self.appearance, |_| 2);
        x += sizes::optional_size(&self.modalias, |x| x.len() as u32);
        x += sizes::optional_size(&self.tx_power, |_| 2);
        x += sizes::optional_size(&self.manufacturer_data, |x| x.len() as u32);
        x += sizes::optional_size(&self.uuids, |x| x.iter().map(|s| s.len() as u32).sum());
        x += sizes::optional_size(&self.service_data, |x| x.iter().map(|s| s.len() as u32).sum());

        x
    }



}