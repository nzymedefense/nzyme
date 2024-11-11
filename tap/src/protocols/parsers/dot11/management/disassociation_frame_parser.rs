use std::sync::Arc;

use anyhow::{bail, Error};
use byteorder::{ByteOrder, LittleEndian};

use crate::helpers::network::to_mac_address_string;
use crate::wireless::dot11::frames::{Dot11DisassociationFrame, Dot11Frame};

pub fn parse(frame: &Arc<Dot11Frame>) -> Result<Dot11DisassociationFrame, Error> {
    if frame.payload.len() < 26 {
        bail!("Disassociation frame payload too short to hold fixed parameters. Discarding.");
    }

    let addr1 = to_mac_address_string(&frame.payload[4..10]);
    let addr2 = to_mac_address_string(&frame.payload[10..16]);
    let addr3 = to_mac_address_string(&frame.payload[16..22]);

    let reason_code = LittleEndian::read_u16(&frame.payload[24..26]);

    Ok(Dot11DisassociationFrame {
        length: frame.length,
        header: frame.header.clone(),
        destination: addr1,
        transmitter: addr2,
        bssid: addr3,
        reason_code 
    })
}