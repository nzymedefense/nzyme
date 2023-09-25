use std::sync::Arc;
use crate::dot11::frames::Dot11BeaconFrame;

pub trait OutputMessageReceiver {
    fn write_dot11_beacon_frame(&self, frame: &Arc<Dot11BeaconFrame>);
}