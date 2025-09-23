use std::sync::{Arc, Mutex};
use crate::configuration::{EngagementTarget, WiFiEngagementInterfaceConfiguration};
use crate::wireless::dot11::engagement::dot11_engagement_capture::Dot11EngagementCapture;
use crate::wireless::dot11::engagement::engagement_control::EngagementInterfaceStatus;

pub struct Dot11EngagementInterface {
    pub name: String,
    pub engage: Vec<EngagementTarget>,
    pub supported_channels_2g: Vec<u16>,
    pub supported_channels_5g: Vec<u16>,
    pub supported_channels_6g: Vec<u16>,
    pub status: EngagementInterfaceStatus,
    pub capture: Arc<Mutex<Option<Arc<Dot11EngagementCapture>>>>
}

impl Dot11EngagementInterface {
    pub fn from_config(name: String, c: WiFiEngagementInterfaceConfiguration) -> Self {
        Dot11EngagementInterface {
            name,
            engage: c.engage,
            supported_channels_2g: c.supported_channels_2g,
            supported_channels_5g: c.supported_channels_5g,
            supported_channels_6g: c.supported_channels_6g,
            status: EngagementInterfaceStatus::Idle,
            capture: Arc::new(Mutex::new(None))
        }
    }
}