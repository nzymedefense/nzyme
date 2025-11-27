use serde::{Deserialize};

#[derive(Debug, Deserialize)]
pub struct FirmwareVersion {
    pub major: u32,
    pub minor: u32
}