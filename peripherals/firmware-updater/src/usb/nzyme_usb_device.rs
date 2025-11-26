#[derive(Debug)]
pub struct NzymeUsbDevice {
    pub bus: u8,
    pub address: u8,
    pub pid: u16,
    pub vid: u16,
    pub serial: String,
    pub acm_port: Option<String>
}