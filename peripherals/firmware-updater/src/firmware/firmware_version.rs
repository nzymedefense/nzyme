use serde::{Deserialize};

#[derive(Debug, Clone, Deserialize, Eq, PartialEq)]
pub struct FirmwareVersion {
    pub major: u32,
    pub minor: u32
}

impl Ord for FirmwareVersion {
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        match self.major.cmp(&other.major) {
            std::cmp::Ordering::Equal => self.minor.cmp(&other.minor),
            other => other,
        }
    }
}

impl PartialOrd for FirmwareVersion {
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        Some(self.cmp(other))
    }
}
