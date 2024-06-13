use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use std::time::Duration;
use anyhow::Error;
use chrono::Utc;
use dbus::arg;
use dbus::arg::{RefArg, Variant};
use dbus::blocking::Connection;
use dbus::blocking::stdintf::org_freedesktop_dbus::Properties;
use log::{info, warn};
use crate::bluetooth::bluetooth_device_advertisement::BluetoothDeviceAdvertisement;
use crate::messagebus::bus::Bus;
use crate::metrics::Metrics;

pub struct Capture {
    pub metrics: Arc<Mutex<Metrics>>,
    pub bus: Arc<Bus>
}

impl Capture {

    pub fn run(&mut self, device_name: &str) {
        loop {
            let bredr_devices = Self::discover_devices(device_name, "bredr");
            let le_devices = Self::discover_devices(device_name, "le");

            info!("{:?}", bredr_devices.unwrap());
            info!("{:?}", le_devices.unwrap());

            // TODO submit to Bluetooth bus for processing.
        }
    }

    pub fn discover_devices(device_name: &str, transport: &str)
        -> Result<HashMap<String, BluetoothDeviceAdvertisement>, Error> {

        /*
         * Using a Map to avoid unlikely case of multiple reports per discovery cycle from
         * same device.
         */
        let mut discovered: HashMap<String, BluetoothDeviceAdvertisement> = HashMap::new();

        // Connect do DBUS.
        let conn = Connection::new_system().unwrap(); // TODO unwrap

        // Obtain bluez reference.
        let adapter_path = "/org/bluez/hci0";
        let adapter = conn.with_proxy(
            "org.bluez",
            adapter_path,
            Duration::from_millis(2500)
        );

        // Set the adapter to not discoverable and not pairable.
        adapter.set(device_name, "Discoverable", false).unwrap(); // TODO unwrap
        adapter.set(device_name, "Pairable", false).unwrap(); // TODO unwrap

        // Set the discovery filter to enable transport for LE
        let mut filter = arg::PropMap::new();
        filter.insert("Transport".to_string(), Variant(Box::new(transport.to_string())));
        adapter.method_call::<(), _, _, _>(device_name, "SetDiscoveryFilter", (filter,)).unwrap();

        // Start bluetooth discovery.
        adapter.method_call::<(), _, _, _>(device_name, "StartDiscovery", ()).unwrap(); // TODO unwrap

        // Sleep to allow discovery.
        std::thread::sleep(Duration::from_secs(30)); // TODO configurable (default: 30)
        // Access the object manager to list all devices
        let obj_manager_path = "/";
        let obj_manager = conn.with_proxy("org.bluez", obj_manager_path, Duration::from_millis(2500));
        #[allow(clippy::complexity)]
            let (devices, ): (HashMap<dbus::Path<'static>, HashMap<String, HashMap<String, Variant<Box<dyn RefArg>>>>>, ) =
            obj_manager.method_call("org.freedesktop.DBus.ObjectManager", "GetManagedObjects", ()).unwrap(); // TODO unwrap


        // Iterate over all discovered devices.
        for (path, interfaces) in devices {
            if path.to_string().starts_with("/org/bluez/hci0/dev_") {
                // Only discovered bluetooth devices.
                if let Some(props) = interfaces.get("org.bluez.Device1") {
                    // Mandatory fields.
                    let mac = Self::parse_mandatory_string_prop(props, "Address");
                    let alias = Self::parse_mandatory_string_prop(props, "Alias");
                    let adapter = Self::parse_mandatory_string_prop(props, "Adapter");

                    let company_id = if let Some(v) = props.get("ManufacturerData") {
                        Self::parse_company_identifier(v)
                    } else {
                        None
                    };


                    // Optional fields.
                    let rssi = Self::parse_optional_i16_prop(props, "RSSI");
                    let tx_power = Self::parse_optional_i16_prop(props, "TxPower");

                    let advertisement = BluetoothDeviceAdvertisement {
                        mac: mac.clone(),
                        name: None,
                        rssi,
                        company_id,
                        alias,
                        class: None,
                        appearance: None,
                        legacy_pairing: None,
                        uuids: None,
                        modalias: None,
                        manufacturer_data: vec![],
                        service_data: vec![],
                        tx_power,
                        adapter,
                        timestamp: Utc::now(),
                    };

                    discovered.insert(mac, advertisement);
                }
            }
        }

        // Stop discovery
        adapter.method_call::<(), _, _, _>(device_name, "StopDiscovery", ()).unwrap(); // TODO unwrap

        Ok(discovered)
    }

    fn parse_mandatory_string_prop(props: &HashMap<String, Variant<Box<dyn RefArg>>>, name: &str)
        -> String {

        if let Some(v) = props.get("Address") {
            match v.as_str() {
                Some(s) => s.to_string(),
                None => {
                    warn!("Invalid Bluetooth advertisement, [{}] not a string: {:?}", name, props);
                    "Invalid".to_string()
                }
            }
        } else {
            warn!("Invalid Bluetooth advertisement, not containing [{}]: {:?}", name, props);
            "Invalid".to_string()
        }
    }

    fn parse_optional_i16_prop(props: &HashMap<String, Variant<Box<dyn RefArg>>>, name: &str)
        -> Option<i16> {

        if let Some(v) = props.get(name) {
            match v.as_i64() {
                Some(x) => Some(x as i16),
                None => {
                    warn!("Invalid Bluetooth advertisement, [{}] not i64: {:?}", name, props);
                    None
                }
            }
        } else {
            None
        }
    }

    fn parse_company_identifier(manufacturer_data_var: &Variant<Box<dyn RefArg>>) -> Option<u16> {
        if let Some(mut iter) = manufacturer_data_var.0.as_iter() {
            match iter.nth(0) {
                Some(key) => key.as_u64().map(|val| val as u16),
                None => None
            }
        } else {
            None
        }
    }

}