use std::sync::Arc;

use anyhow::{Error, bail};
use byteorder::{LittleEndian, ByteOrder};

use crate::{dot11::frames::{Dot11Frame, Dot11DeauthenticationFrame}, helpers::network::to_mac_address_string};

pub fn parse(frame: &Arc<Dot11Frame>) -> Result<Dot11DeauthenticationFrame, Error> {
    if frame.payload.len() < 26 {
        bail!("Deauthentication frame payload too short to hold fixed parameters. Discarding.");
    }

    let addr1 = to_mac_address_string(&frame.payload[4..10]);
    let addr2 = to_mac_address_string(&frame.payload[10..16]);
    let addr3 = to_mac_address_string(&frame.payload[16..22]);

    let reason_code = LittleEndian::read_u16(&frame.payload[24..26]);

    Ok(Dot11DeauthenticationFrame {
        length: frame.length,
        header: frame.header.clone(),
        destination: addr1,
        transmitter: addr2,
        bssid: addr3,
        reason_code 
    })
}