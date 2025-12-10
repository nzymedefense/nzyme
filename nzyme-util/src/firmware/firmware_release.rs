use crate::firmware::firmware_version::FirmwareVersion;

#[allow(dead_code)]
#[derive(Debug)]
pub struct FirmwareRelease {
    pub version: FirmwareVersion,
    pub firmware_url: String,
    pub firmware_signature_url: String,
    pub signed_with: String,
    pub release_notes: String
}