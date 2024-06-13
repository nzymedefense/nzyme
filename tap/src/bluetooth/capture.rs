use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use std::time::Duration;
use dbus::arg::{Dict, RefArg, Variant};
use dbus::blocking::Connection;
use log::info;
use crate::messagebus::bus::Bus;
use crate::metrics::Metrics;

pub struct Capture {
    pub metrics: Arc<Mutex<Metrics>>,
    pub bus: Arc<Bus>
}

impl Capture {

    pub fn run(&mut self, device_name: &str) -> Result<(), Box<dyn std::error::Error>>  {
        loop {
            // Connect do DBUS.
            let conn = Connection::new_system()?;

            // Obtain bluez reference.
            let adapter_path = "/org/bluez/hci0";
            let adapter = conn.with_proxy(
                "org.bluez", 
                adapter_path, 
                Duration::from_millis(1000)
            );

            // Start bluetooth discovery.
            adapter.method_call(device_name, "StartDiscovery", ())?;

            // Sleep to allow discovery.
            std::thread::sleep(Duration::from_secs(10));

            // Access the object manager to list all Bluetooth devices
            let obj_manager_path = "/";
            let obj_manager = conn.with_proxy("org.bluez", obj_manager_path, Duration::from_millis(1000));
            let (devices, ): (HashMap<dbus::Path<'static>, HashMap<String, HashMap<String, Variant<Box<dyn RefArg>>>>>, ) =
                obj_manager.method_call("org.freedesktop.DBus.ObjectManager", "GetManagedObjects", ())?;

            // Iterate over all discovered devices.
            for (path, interfaces) in devices {
                if path.to_string().starts_with("/org/bluez/hci0/dev_") {
                    if let Some(props) = interfaces.get("org.bluez.Device1") {
                        let address = props.get("Address").and_then(|v| v.as_str()).unwrap_or("Unknown Address");
                        let name = props.get("Name").and_then(|v| v.as_str()).unwrap_or("Unknown Device");
                        info!("Bluetooth device: {} ({})", name, address);

                        // TODO submit to Bluetooth bus for processing.
                    }
                }
            }

            // Stop discovery
            adapter.method_call(device_name, "StopDiscovery", ())?;
        }

        Ok(())
    }

}