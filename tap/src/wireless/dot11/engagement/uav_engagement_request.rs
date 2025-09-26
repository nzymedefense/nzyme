use crate::wireless::dot11::supported_frequency::SupportedChannelWidth;

#[derive(Clone, Debug)]
pub struct UavEngagementRequest {
    pub uav_id: String,
    pub mac_address: String,
    pub initial_frequency: u16,
    pub initial_channel_width: SupportedChannelWidth,
}