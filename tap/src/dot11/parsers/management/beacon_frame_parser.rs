use std::sync::Arc;

use byteorder::{LittleEndian, ByteOrder};
use log::{info, trace};

use crate::dot11::frames::{Dot11Frame, Dot11BeaconFrame};

pub fn parse(frame: &Arc<Dot11Frame>) { //-> Result<Dot11BeaconFrame, Error> {

    if (frame.payload.len() < 12) {
        trace!("Beacon frame payload too short to hold fixed parameters. Discarding.");
        return
    }

    info!("destination: {:x?}", &frame.payload[4..10]);
    info!("transmitter: {:x?}", &frame.payload[10..16]);

    let timestamp = LittleEndian::read_u64(&frame.payload[24..33]);
    let interval = LittleEndian::read_u16(&frame.payload[33..35]);

    info!("ts: {}, int: {}", timestamp, interval);

}