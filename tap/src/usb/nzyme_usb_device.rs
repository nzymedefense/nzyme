#[derive(Debug)]
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

#[derive(Debug, Clone)]
pub struct FirmwareVersion {
    pub major: u32,
    pub minor: u32
}