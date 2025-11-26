use anyhow::{bail, Context, Result};
use rusb::{Context as UsbContextType, Device, DeviceDescriptor, UsbContext};
use crate::usb::nzyme_usb_device::NzymeUsbDevice;

const NZYME_VID: u16 = 0x390C;

pub fn detect_nzyme_usb_devices() -> Result<Vec<NzymeUsbDevice>>  {
    let context = UsbContextType::new()?;
    let devices = context.devices()?;

    let mut nzyme_devices = Vec::new();

    for device in devices.iter() {
        let desc = device.device_descriptor()?;

        if desc.vendor_id() == NZYME_VID {
            nzyme_devices.push(build_nzyme_device_info(&device, &desc)?);
        }
    }

    Ok(nzyme_devices)
}

fn build_nzyme_device_info(device: &Device<UsbContextType>, desc: &DeviceDescriptor)
    -> Result<NzymeUsbDevice> {

    let bus = device.bus_number();
    let address = device.address();
    let vid = desc.vendor_id();
    let pid = desc.product_id();

    // Try to open the device to read string descriptors (may fail if permissions are missing)
    let (serial, acm_port) = match device.open() {
        Ok(handle) => {
            let serial = handle
                .read_serial_number_string_ascii(desc)
                .unwrap_or_else(|_| "<unavailable>".to_string());

            let tty = find_tty_for_usb_device(bus, address)
                .with_context(|| "Failed to enumerate tty devices")?;

            if let Some(tty) = tty {
                (serial, Some(tty))
            } else {
                (serial, None)
            }
        }
        Err(e) => {
            bail!("Could not open USB device. Make sure you have sufficient permissions.")
        }
    };

    Ok(NzymeUsbDevice { bus, address, pid, vid, serial, acm_port })
}

fn find_tty_for_usb_device(bus: u8, address: u8) -> Result<Option<String>> {
    let mut enumerator = udev::Enumerator::new()?;
    enumerator.match_subsystem("tty")?;

    for dev in enumerator.scan_devices()? {
        let sys_name = match dev.sysname().to_str() {
            Some(s) => s,
            None => continue,
        };

        if !sys_name.starts_with("ttyACM") {
            continue;
        }

        let parent = match dev.parent_with_subsystem_devtype("usb", "usb_device") {
            Ok(Some(device)) => device,
            Ok(None) => continue,  // No matching parent.
            Err(_) => continue, // Skip over errors.
        };

        let bus_num = parent
            .attribute_value("busnum")
            .and_then(|s| s.to_str())
            .and_then(|s| s.parse::<u8>().ok());

        let dev_num = parent
            .attribute_value("devnum")
            .and_then(|s| s.to_str())
            .and_then(|s| s.parse::<u8>().ok());

        if let (Some(bus_num), Some(dev_num)) = (bus_num, dev_num) {
            if bus_num == bus && dev_num == address {
                // Found it. We currently only have devices that spawn one TTY so we can return.
                return Ok(Some(format!("/dev/{sys_name}")))
            }
        }
    }

    Ok(None)
}