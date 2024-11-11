use std::sync::Arc;

use anyhow::{bail, Error};
use bitvec::{order::Lsb0, view::BitView};

use crate::helpers::network::to_mac_address_string;
use crate::wireless::dot11::frames::{Dot11DSInformation, Dot11DataFrame, Dot11DataFrameDirection, Dot11Frame};

pub fn parse(frame: &Arc<Dot11Frame>) -> Result<Dot11DataFrame, Error> {
    if frame.payload.len() < 24 {
        bail!("Data frame payload too short to hold fixed parameters. Discarding.");
    };

    // Parse relevant frame control flags.
    let fcs_bmask = &frame.payload[1].view_bits::<Lsb0>();

    let to_ds = *fcs_bmask.get(0).unwrap();
    let from_ds = *fcs_bmask.get(1).unwrap();

    let addr1 = to_mac_address_string(&frame.payload[4..10]);
    let addr2 = to_mac_address_string(&frame.payload[10..16]);
    let addr3 = to_mac_address_string(&frame.payload[16..22]);

    let (destination, source, bssid, direction) = match (to_ds, from_ds) {
        (true, true) => (addr3, addr2, String::new(), Dot11DataFrameDirection::WDS),
        (true, false) => (addr3, addr2, addr1, Dot11DataFrameDirection::Leaving),
        (false, true) => (addr1, addr3, addr2, Dot11DataFrameDirection::Entering),
        (false, false) => (addr1, addr2, addr3, Dot11DataFrameDirection::NotLeavingOrAdHoc)
    };

    Ok(Dot11DataFrame {
        length: frame.length,
        header: frame.header.clone(),
        ds: Dot11DSInformation {
            destination,
            source,
            bssid,
            direction
        }
    })
}