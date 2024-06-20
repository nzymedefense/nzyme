use std::collections::HashMap;
use std::panic::catch_unwind;
use std::sync::{Arc, Mutex};
use std::thread::sleep;
use std::time::Duration;
use anyhow::{Context, Error};
use chrono::Utc;
use dbus::arg;
use dbus::arg::{RefArg, Variant};
use dbus::blocking::Connection;
use dbus::blocking::stdintf::org_freedesktop_dbus::Properties;
use log::{debug, error, info, warn};
use crate::bluetooth::bluetooth_device_advertisement::BluetoothDeviceAdvertisement;
use crate::configuration::BluetoothInterface;
use crate::messagebus::bus::Bus;
use crate::messagebus::channel_names::BluetoothChannelName;
use crate::metrics::Metrics;
use crate::to_pipeline;

pub struct Capture {
    pub metrics: Arc<Mutex<Metrics>>,
    pub bus: Arc<Bus>,
    pub configuration: BluetoothInterface
}

const DBUS_INTERFACE: &str = "org.bluez.Adapter1";

impl Capture {

    pub fn run(&mut self, device_name: &str) {
        loop {
            let result = catch_unwind(|| {
                if self.configuration.bt_classic_enabled {
                    match self.discover_devices(device_name, "bredr") {
                        Ok(devices) => self.discovered_devices_to_pipeline(device_name, devices),
                        Err(e) => {
                            error!("Could not discover Bluetooth Classic devices: {}", e);
                        }
                    }
                }

                if self.configuration.bt_le_enabled {
                    match self.discover_devices(device_name, "le") {
                        Ok(devices) => self.discovered_devices_to_pipeline(device_name, devices),
                        Err(e) => {
                            error!("Could not discover Bluetooth LE devices: {}", e);
                        }
                    }
                }
            });

            if let Err(e) = result {
                if let Some(s) = e.downcast_ref::<&str>() {
                    error!("Could not discover Bluetooth devices: {}", s);
                } else if let Some(s) = e.downcast_ref::<String>() {
                    error!("Could not discover Bluetooth devices: {}", s);
                } else {
                    error!("Could not discover Bluetooth devices. Panicked with an unknown type.");
                }
            }

            /*
             * The discovery methods sleep during discovery, but we add another sleep to make sure
             * we don't empty spin in case of errors early in the discovery methods.
             */
            sleep(Duration::from_secs(1))
        }
    }

    pub fn discover_devices(&self, device_name: &str, transport: &str)
        -> Result<HashMap<String, BluetoothDeviceAdvertisement>, Error> {

        /*
         * Using a Map to avoid unlikely case of multiple reports per discovery cycle from
         * same device.
         */
        let mut discovered: HashMap<String, BluetoothDeviceAdvertisement> = HashMap::new();

        // Connect do D-Bus.
        let conn = Connection::new_system().context("Could not establish connection to D-Bus")?;

        // Obtain bluez reference.
        let adapter = conn.with_proxy(
            "org.bluez",
            format!("/org/bluez/{}", device_name),
            Duration::from_secs(self.configuration.dbus_method_call_timeout_seconds as u64)
        );

        // Set the adapter to not discoverable and not pairable.
        adapter.set(DBUS_INTERFACE, "Discoverable", false)
            .context("Could not set Discoverable=false on device")?;
        adapter.set(DBUS_INTERFACE, "Pairable", false)
            .context("Could not set Pairable=false on device")?;

        // Set the discovery filter to set transport to selected method.
        let mut filter = arg::PropMap::new();
        filter.insert("Transport".to_string(), Variant(Box::new(transport.to_string())));
        adapter.method_call::<(), _, _, _>(DBUS_INTERFACE, "SetDiscoveryFilter", (filter,))
            .context("Could not set discovery filter")?;

        // Start bluetooth discovery.
        adapter.method_call::<(), _, _, _>(DBUS_INTERFACE, "StartDiscovery", ())
            .context("Could not start discovery")?;

        // Sleep to allow discovery.
        std::thread::sleep(Duration::from_secs(self.configuration.discovery_period_seconds as u64));

        // Access the object manager to list all devices
        let obj_manager_path = "/";
        let obj_manager = conn.with_proxy(
            "org.bluez",
            obj_manager_path,
            Duration::from_secs(self.configuration.dbus_method_call_timeout_seconds as u64)
        );
        #[allow(clippy::complexity)]
        let (devices, ): (HashMap<dbus::Path<'static>, HashMap<String, HashMap<String, Variant<Box<dyn RefArg>>>>>, ) =
            obj_manager.method_call("org.freedesktop.DBus.ObjectManager", "GetManagedObjects", ())
                .context("Could not fetch devices from object manager")?;

        // Iterate over all discovered devices.
        for (path, interfaces) in devices {
            if path.to_string().starts_with(&format!("/org/bluez/{}/dev_", device_name)) {
                // Only discovered bluetooth devices.
                if let Some(props) = interfaces.get("org.bluez.Device1") {
                    /*
                     * Is this device connected to the Bluetooth adapter we are listening on?
                     * Notify in log because this will negatively impact discovery. This is most
                     * like a bluetooth device connected to the machine this tap runs on. The user
                     * should either disconnect (and forget) the device or use another bluetooth
                     * adapter that has no devices connected.
                     */
                    if let Some(true) = Self::parse_optional_bool_prop(props, "Connected") {
                        warn!("Bluetooth adapter [{}] has a connected device. This will \
                        negatively impact discovery. Disconnected and un-pair all bluetooth \
                        devices from this machine. Device: {:?}", device_name, props)
                    }

                    // Mandatory fields.
                    let mac = Self::parse_mandatory_string_prop(props, "Address");
                    let alias = Self::parse_mandatory_string_prop(props, "Alias");

                    // Optional fields.
                    let rssi = Self::parse_optional_i16_prop(props, "RSSI");
                    let tx_power = Self::parse_optional_i16_prop(props, "TxPower");
                    let name = Self::parse_optional_string_prop(props, "Name");
                    let class = Self::parse_optional_u32_prop(props, "Class");
                    let appearance = Self::parse_optional_u32_prop(props, "Appearance");
                    let modalias = Self::parse_optional_string_prop(props, "Modalias");
                    let uuids = Self::parse_optional_string_vector(props, "UUIDs");
                    let service_data = Self::parse_optional_string_vector(props, "ServiceData");

                    // Manufacturer data incl. company ID.
                    let (company_id, manufacturer_data) = if let Some(v) = props.get("ManufacturerData") {
                        Self::parse_manufacturer_data(v)
                    } else {
                        (None, None)
                    };

                    discovered.insert(mac.clone(), BluetoothDeviceAdvertisement {
                        mac,
                        name,
                        rssi,
                        company_id,
                        alias,
                        class,
                        appearance,
                        modalias,
                        tx_power,
                        manufacturer_data,
                        uuids,
                        service_data,
                        device: device_name.to_string(),
                        transport: transport.to_string(),
                        timestamp: Utc::now(),
                    });
                }
            }
        }

        // Stop discovery
        adapter.method_call::<(), _, _, _>(DBUS_INTERFACE, "StopDiscovery", ())
            .context("Could not stop discovery")?;

        Ok(discovered)
    }

    fn discovered_devices_to_pipeline(&self,
                                      device_name: &str,
                                      devices: HashMap<String, BluetoothDeviceAdvertisement>) {
        for device in devices.values() {
            to_pipeline!(
                BluetoothChannelName::BluetoothDevicesPipeline,
                self.bus.bluetooth_device_pipeline.sender,
                Arc::new(device.clone()),
                1024
            );

            match self.metrics.lock() {
                Ok(mut metrics) => {
                    metrics.increment_processed_bytes_total(device.estimate_struct_size());
                    metrics.update_capture(device_name, true, 0, 0);
                },
                Err(e) => error!("Could not acquire metrics mutex: {}", e)
            }
        }
    }

    fn parse_mandatory_string_prop(props: &HashMap<String, Variant<Box<dyn RefArg>>>, name: &str)
        -> String {

        Self::parse_optional_string_prop(props, name).unwrap_or_else(|| {
            warn!("Invalid Bluetooth advertisement, not containing [{}]: {:?}", name, props);
            "Invalid".to_string()
        })
    }

    fn parse_optional_string_prop(props: &HashMap<String, Variant<Box<dyn RefArg>>>, name: &str)
                                  -> Option<String> {
        props.get(name).and_then(|v|
        v.as_str()
            .map(|val| val.to_string())
            .or_else(|| {
                warn!("Invalid Bluetooth advertisement, [{}] not a string: {:?}", name, props);
                None
            })
        )
    }

    fn parse_optional_i16_prop(props: &HashMap<String, Variant<Box<dyn RefArg>>>, name: &str)
        -> Option<i16> {
        props.get(name).and_then(|v| {
            match v.as_i64() {
                Some(val) if val <= i16::MAX as i64 => Some(val as i16),
                Some(_) => {
                    warn!("Invalid Bluetooth advertisement, [{}] value out of range for i64: {:?}", name, props);
                    None
                },
                None => {
                    warn!("Invalid Bluetooth advertisement, [{}] not u64 or not present: {:?}", name, props);
                    None
                }
            }
        })
    }

    fn parse_optional_u32_prop(props: &HashMap<String, Variant<Box<dyn RefArg>>>, name: &str)
        -> Option<u32> {
        props.get(name).and_then(|v| {
            match v.as_u64() {
                Some(val) if val <= u32::MAX as u64 => Some(val as u32),
                Some(_) => {
                    warn!("Invalid Bluetooth advertisement, [{}] value out of range for u32: {:?}", name, props);
                    None
                },
                None => {
                    warn!("Invalid Bluetooth advertisement, [{}] not u64 or not present: {:?}", name, props);
                    None
                }
            }
        })
    }

    fn parse_optional_bool_prop(props: &HashMap<String, Variant<Box<dyn RefArg>>>, name: &str)
        -> Option<bool> {

        props.get(name).and_then(|v| {
            v.0.as_any().downcast_ref::<bool>().copied().or_else(|| {
                warn!("Invalid Bluetooth advertisement, [{}] not bool: {:?}", name, props);
                None
            })
        })
    }

    fn parse_optional_string_vector(props: &HashMap<String, Variant<Box<dyn RefArg>>>, name: &str)
        -> Option<Vec<String>> {

        if let Some(v) = props.get(name) {
            match v.0.as_iter() {
                Some(iter) => {
                    let mut data = Vec::new();
                    for val in iter {
                        match val.as_str() {
                            Some(str) => {
                                data.push(str.to_string())
                            },
                            None => {
                                // Ignore non-string elements.
                                debug!("Invalid Bluetooth advertisement, [{}] includes \
                                element that is not a string: {:?}", name, props);
                            }
                        }
                    }

                    if data.is_empty() {
                        None
                    } else {
                        Some(data)
                    }
                },
                None => None
            }
        } else {
            None
        }
    }

    fn parse_manufacturer_data(manufacturer_data_var: &Variant<Box<dyn RefArg>>)
        -> (Option<u16>, Option<Vec<u8>>) {

        let company_id = if let Some(mut iter) = manufacturer_data_var.0.as_iter() {
            match iter.nth(0) {
                Some(key) => key.as_u64().map(|val| val as u16),
                None => None
            }
        } else {
            None
        };

        let data = manufacturer_data_var.0.as_iter().and_then(|mut iter| {
            iter.nth(1)?.as_iter().and_then(|mut iter| {
                iter.nth(0)?.as_iter().map(|iter| {
                    iter.filter_map(|val| val.as_u64().map(|v| v as u8)).collect::<Vec<u8>>()
                })
            })
        });

        (company_id, data)
    }

}