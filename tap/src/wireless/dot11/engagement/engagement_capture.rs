use anyhow::Error;
use chrono::{DateTime, Utc};
use crate::wireless::dot11::engagement::engagement_control::EngagementInterfaceStatus;
use crate::wireless::dot11::engagement::uav_engagement_request::UavEngagementRequest;

pub trait EngagementCapture: Send + Sync {
    fn run(&self);
    fn engage_uav_target(&self, target: &UavEngagementRequest) -> Result<(), Error>;
    fn seek_current_target(&self) -> Result<(), Error>;
    fn reengage_current_target(&self) -> Result<(), Error>;
    fn disengage_current_target(&self) -> Result<(), Error>;

    fn current_status(&self) -> EngagementInterfaceStatus;
    fn set_status(&self, new: EngagementInterfaceStatus) -> Result<(), Error>;

    fn current_target(&self) -> Option<UavEngagementRequest>;
    fn set_current_target(&self, new: Option<UavEngagementRequest>) -> Result<(), Error>;

    fn last_tracked_frame_timestamp(&self) -> Option<DateTime<Utc>>;

}