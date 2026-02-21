use anyhow::{bail, Context, Error, Result};
use rusb::{Context as UsbContextType, Device, DeviceDescriptor, UsbContext};
use crate::firmware::firmware_version::FirmwareVersion;
use crate::usb::nzyme_usb_device::NzymeUsbDevice;

pub const NZYME_VID: u16 = 0x390C;

pub fn find_bootloader_of_pid(pid: u16) -> Result<u16> {
    match pid {
        // Limina.
        0x0200 | 0x0001 => Ok(0x0001),

        // Sona. (WiFi, Bluetooth, 802.15.4)
        0x0100 | 0x0101 | 0x0102 | 0x0002 => Ok(0x0002),

        // Axia.
        0x0400 | 0x0004 => Ok(0x0004),

        _ => bail!("Unknown PID <{}>.", pid)
    }
}

pub fn is_sona(pid: u16) -> bool {
    pid == 0x0100 || pid == 0x0101 || pid == 0x0102
}

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
        minor: dv.minor() as u32
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

pub fn nzyme_device_is_connected(serial_number: &str, vid: u16, pid: u16) -> Result<bool, Error> {
    Ok(detect_nzyme_usb_devices()?
        .into_iter()
        .find(|d| d.serial == serial_number && d.vid == vid && d.pid == pid)
        .is_some())
}