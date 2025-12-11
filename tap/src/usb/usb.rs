use anyhow::{bail, Context, Result};
use rusb::{Context as UsbContextType, Device, DeviceDescriptor, UsbContext};
use crate::usb::nzyme_usb_device::{FirmwareVersion, NzymeUsbDevice};

const NZYME_VID: u16 = 0x390C;

pub fn find_first_nzyme_usb_device_with_device_id(device_id: u16) -> Result<Option<NzymeUsbDevice>> {
    for device in detect_nzyme_usb_devices()? {
        if device.pid == device_id {
            return Ok(Some(device))
        }
    }

    Ok(None)
}

fn detect_nzyme_usb_devices() -> Result<Vec<NzymeUsbDevice>>  {
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

    // Version. (from `bcdDevice` parameter)
    let dv = desc.device_version();
    let firmware_version = FirmwareVersion {
        major: dv.major() as u32,
        minor: dv.sub_minor() as u32 // `rusb` is inventing a patch/sub-minor version that the
        // USB spec does not define. We'll use it as minor.
    };

    // Try to open the device to read string descriptors. (may fail if permissions are missing)
    let (product, manufacturer, serial, acm_port) = match device.open() {
        Ok(handle) => {
            let serial = read_usb_string_with_retry(&handle, desc, "serial");
            let manufacturer = read_usb_string_with_retry(&handle, desc, "manufacturer");
            let product = read_usb_string_with_retry(&handle, desc, "product");

            let tty = find_tty_for_usb_device(bus, address)
                .with_context(|| "Failed to enumerate tty devices")?;

            (product, manufacturer, serial, tty)
        }
        Err(e) => {
            bail!("Could not open USB device. Make sure you have sufficient permissions: {}", e)
        }
    };

    Ok(NzymeUsbDevice {
        product, manufacturer, serial, firmware_version, pid, vid, bus, address, acm_port
    })
}

// Sometimes, we get transient errors, but it usually works on retry. USB OS/driver oddness.
fn read_usb_string_with_retry(handle: &rusb::DeviceHandle<UsbContextType>,
                              desc: &DeviceDescriptor,
                              which: &str) -> String {
    const MAX_RETRIES: u8 = 5;

    for _ in 1..=MAX_RETRIES {
        let result = match which {
            "serial" => handle.read_serial_number_string_ascii(desc),
            "manufacturer" => handle.read_manufacturer_string_ascii(desc),
            "product" => handle.read_product_string_ascii(desc),
            _ => Err(rusb::Error::Other),
        };

        match result {
            Ok(s) => {
                if s != "?" {
                    return s
                } else {
                    std::thread::sleep(std::time::Duration::from_millis(200));
                    continue;
                }
            },
            Err(_) => {
                std::thread::sleep(std::time::Duration::from_millis(200));
                continue;
            }
        }
    }

    "<unavailable>".to_string()
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