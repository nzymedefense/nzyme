use crate::connect::connect_firmware_directory::Peripheral;
use crate::firmware::firmware_release::FirmwareRelease;
use crate::firmware::firmware_version::FirmwareVersion;

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

impl NzymeUsbDevice {

    pub fn most_recent_firmware_release_available(&self, firmware_directory: &Vec<Peripheral>)
        -> Option<FirmwareRelease> {

        for peripheral in firmware_directory {
            if peripheral.vid == self.vid as u32 && peripheral.did == self.pid as u32 {
                let p = peripheral.clone();
                return Some(FirmwareRelease {
                    version: p.version,
                    firmware_url: p.firmware_url,
                    firmware_signature_url: p.firmware_signature_url,
                    signed_with: p.signed_with,
                    release_notes: p.release_notes
                })
            }
        }

        None
    }

    pub fn has_outdated_firmware(&self, firmware_directory: &Vec<Peripheral>) -> bool {
        let release = self.most_recent_firmware_release_available(firmware_directory);

        match release {
            Some(release) => release.version > self.firmware_version,
            None => false
        }
    }

}