use std::fmt;
use crate::firmware::firmware_version::FirmwareVersion;

#[derive(Debug, Clone)]
pub struct NzymeUsbDevice {
    pub product: String,
    pub manufacturer: String,
    pub serial: String,
    pub firmware_version: FirmwareVersion,
    pub pid: u16,
    pub vid: u16,
    pub bus: u8,
    pub address: u8,
    pub acm_port: Option<String>,
}

impl fmt::Display for NzymeUsbDevice {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        // ANSI escape codes
        const BOLD: &str = "\x1b[1m";
        const RESET: &str = "\x1b[0m";

        write!(
            f,
            "Device:\n\
             ├─ Product:          {bold}{}{reset}\n\
             ├─ Manufacturer:     {}\n\
             ├─ Serial:           {bold}{}{reset}\n\
             ├─ Firmware Version: {bold}v{}.{}{reset}\n\
             ├─ VID:PID:          {:04X}:{:04X}\n\
             ├─ Bus / Address:    {} / {}\n\
             └─ ACM Port:         {}",
            self.product,
            self.manufacturer,
            self.serial,
            self.firmware_version.major,
            self.firmware_version.minor,
            self.vid,
            self.pid,
            self.bus,
            self.address,
            self.acm_port.as_deref().unwrap_or("<none>"),
            bold = BOLD,
            reset = RESET,
        )
    }
}