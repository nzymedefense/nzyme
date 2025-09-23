use anyhow::{bail, Error};
use caps::{CapSet, Capability};
use log::{info};
use crate::wireless::dot11::nl::InterfaceState::{Down, Up};
use crate::wireless::dot11::nl::Nl;

pub fn prepare_device(device_name: &str) -> Result<(), Error> {
    // Check if `net_admin` permission is set on this program.
    let permission = caps::has_cap(None, CapSet::Permitted, Capability::CAP_NET_ADMIN);
    match permission {
        Ok(result) => {
            if !result {
                bail!("Missing `net_admin` permission on this program. Please follow the \
                        documentation.");
            }
        }
        Err(e) => {
            bail!("Could not check program capabilities: {}", e);
        }
    }

    let mut nl = match Nl::new() {
        Ok(nl) => nl,
        Err(e) => {
            bail!("Could not establish Netlink connection: {}", e);
        }
    };

    info!("Temporarily disabling interface [{}] ...", device_name);
    match nl.change_80211_interface_state(&device_name.to_string(), Down) {
        Ok(_) => info!("Device [{}] is now down.", device_name),
        Err(e) => {
            bail!("Could not disable device [{}]: {}", device_name, e);
        }
    }

    info!("Enabling monitor mode on interface [{}] ...", device_name);
    match nl.enable_monitor_mode(&device_name.to_string()) {
        Ok(_) => info!("Device [{}] is now in monitor mode.", device_name),
        Err(e) => {
            bail!("Could not set device [{}] to monitor mode: {}", device_name, e);
        }
    }

    info!("Enabling interface [{}] ...", device_name);
    match nl.change_80211_interface_state(&device_name.to_string(), Up) {
        Ok(_) => info!("Device [{}] is now up.", device_name),
        Err(e) => {
            bail!("Could not enable device [{}]: {}", device_name, e);
        }
    }

    Ok(())
}