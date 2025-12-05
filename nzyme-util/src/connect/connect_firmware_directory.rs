use anyhow::Result;
use reqwest::blocking;
use serde::{Deserialize};
use crate::firmware::firmware_version::FirmwareVersion;

const PERIPHERALS_URL: &str = "https://api.connect.nzyme.org/peripherals/firmware/versions";

#[derive(Debug, Clone, Deserialize)]
pub struct Peripheral {
    #[serde(rename = "peripheral_id")]
    pub id: String,
    #[serde(rename = "peripheral_name")]
    pub name: String,
    #[serde(rename = "peripheral_vid")]
    pub vid: u32,
    #[serde(rename = "peripheral_did")]
    pub did: u32,
    pub version: FirmwareVersion,
    pub firmware_url: String,
    pub firmware_signature_url: String,
    pub signed_with: String,
    pub release_notes: String,
}

pub fn fetch_firmware_directory() -> Result<Vec<Peripheral>> {
    let resp = blocking::get(PERIPHERALS_URL)?;
    let peripherals = resp.json::<Vec<Peripheral>>()?;
    Ok(peripherals)
}

