use anyhow::Result;
use reqwest::blocking;
use serde::{Deserialize, Serialize};
use crate::firmware::firmware_version::FirmwareVersion;

const PERIPHERALS_URL: &str = "https://api.connect.nzyme.org/peripherals/firmware/versions";

#[derive(Debug, Deserialize)]
pub struct Peripheral {
    peripheral_id: String,
    peripheral_name: String,
    peripheral_vid: u32,
    peripheral_did: u32,
    version: FirmwareVersion,
    firmware_url: String,
    firmware_signature_url: String,
    signed_with: String,
    release_notes: String,
}

pub fn fetch_firmware_directory() -> Result<Vec<Peripheral>> {
    let resp = blocking::get(PERIPHERALS_URL)?;
    let peripherals = resp.json::<Vec<Peripheral>>()?;
    Ok(peripherals)
}

