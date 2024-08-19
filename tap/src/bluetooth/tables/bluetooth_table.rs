use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use bitvec::order::Msb0;
use bitvec::view::BitView;
use log::{error, info};
use crate::bluetooth::bluetooth_device_advertisement::BluetoothDeviceAdvertisement;
use crate::bluetooth::detection::device_tagger::tag_device_advertisement;
use crate::bluetooth::tables::bluetooth_device::BluetoothDevice;
use crate::link::leaderlink::Leaderlink;
use crate::link::reports::bluetooth_devices_report;
use crate::metrics::Metrics;

pub struct BluetoothTable {
    metrics: Arc<Mutex<Metrics>>,
    leaderlink: Arc<Mutex<Leaderlink>>,
    devices: Mutex<HashMap<String, BluetoothDevice>>
}

impl BluetoothTable {

    pub fn new(metrics: Arc<Mutex<Metrics>>, leaderlink: Arc<Mutex<Leaderlink>>) -> Self {
        Self {
            metrics,
            leaderlink,
            devices: Mutex::new(HashMap::new())
        }
    }

    pub fn register_device_advertisement(&self, advertisement: Arc<BluetoothDeviceAdvertisement>) {
        if advertisement.rssi.is_none() {
            return
        }

        tag_device_advertisement(&advertisement);

        match self.devices.lock() {
            Ok(mut devices) => {
                match devices.get_mut(&advertisement.mac) {
                    Some(device) => {
                        // Device already exists. Update last_seen.
                        device.last_seen = advertisement.timestamp;
                    },
                    None => {
                        // New device. Insert.
                        devices.insert(advertisement.mac.clone(), advertisement.into());
                    }
                }
            },
            Err(e) => error!("Could not acquire bluetooth table mutex: {}", e)
        }
    }

    pub fn process_report(&self) {
        match self.devices.lock() {
            Ok(mut devices) => {
                // Generate JSON.
                let report = match serde_json::to_string(
                    &bluetooth_devices_report::generate_report(&devices)) {

                    Ok(report) => report,
                    Err(e) => {
                        error!("Could not serialize Bluetooth devices report: {}", e);
                        return;
                    }
                };

                // Send report.
                match self.leaderlink.lock() {
                    Ok(link) => {
                        if let Err(e) = link.send_report("bluetooth/devices", report) {
                            error!("Could not submit Bluetooth devices report: {}", e);
                        }
                    },
                    Err(e) => error!("Could not acquire Bluetooth table lock for report submission: {}", e)
                }

                // Clean up.
                devices.clear();
            },
            Err(e) => error!("Could not acquire Bluetooth table lock for report generation {}", e)
        }
    }

    pub fn calculate_metrics(&self) {
        let devices_size: i128 = match self.devices.lock() {
            Ok(devices) => devices.len() as i128,
            Err(e) => {
                error!("Could not acquire mutex to calculate Bluetooth device table size: {}", e);

                -1
            }
        };

        match self.metrics.lock() {
            Ok(mut metrics) => {
                metrics.set_gauge("tables.bluetooth.devices.size", devices_size);
            },
            Err(e) => error!("Could not acquire metrics mutex: {}", e)
        }
    }

}
