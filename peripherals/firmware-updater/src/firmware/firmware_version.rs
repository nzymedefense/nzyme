use serde::{Deserialize, Serialize};

#[derive(Debug, Deserialize)]
pub struct FirmwareVersion {
    major: u32,
    minor: u32,
    patch: u32,
}