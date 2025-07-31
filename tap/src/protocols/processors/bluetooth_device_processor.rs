use std::sync::{Arc, Mutex};
use log::error;
use crate::wireless::bluetooth::bluetooth_device_advertisement::BluetoothDeviceAdvertisement;
use crate::wireless::bluetooth::tables::bluetooth_table::BluetoothTable;

pub struct BluetoothDeviceProcessor {
    bluetooth_table: Arc<Mutex<BluetoothTable>>
}

impl BluetoothDeviceProcessor {

    pub fn new(bluetooth_table: Arc<Mutex<BluetoothTable>>) -> Self {
        Self {
            bluetooth_table
        }
    }

    pub fn process(&self, device: Arc<BluetoothDeviceAdvertisement>) {
        match self.bluetooth_table.lock() {
            Ok(table) => table.register_device_advertisement(device),
            Err(e) => { error!("Could not acquire Bluetooth table lock: {}", e); }
        }
    }

}