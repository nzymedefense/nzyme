use std::sync::Arc;

use log::info;

use crate::dot11::frames::Dot11Frame;

pub struct Dot11FrameProcessor {

}

impl Dot11FrameProcessor {

    pub fn process(&mut self, frame: &Arc<Dot11Frame>) {

        info!("frame processor: {:?}", frame)

    }

}
