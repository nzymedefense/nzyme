use std::sync::{Arc};

use crate::{ethernet::packets::UDPPacket};

pub struct UDPProcessor {
}

impl UDPProcessor {

    pub fn new() -> Self {
        Self {}
    }

    pub fn process(&mut self, packet: &Arc<UDPPacket>) {
        // TODO
    }

}