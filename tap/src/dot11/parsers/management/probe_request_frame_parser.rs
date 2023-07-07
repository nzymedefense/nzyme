use std::sync::Arc;

use anyhow::{Error, bail};
use log::trace;

use crate::{dot11::frames::{Dot11Frame, Dot11ProbeRequestFrame}, helpers::network::to_mac_address_string};

pub fn parse(frame: &Arc<Dot11Frame>) -> Result<Dot11ProbeRequestFrame, Error> {
    if frame.length < 16 {
        bail!("Probe request frame payload too short to hold fixed parameters. Discarding.");
    }

    let transmitter = to_mac_address_string(&frame.payload[10..16]);
    let mut ssid: Option<String> = Option::None;

    let mut cursor: usize = 24;
    if frame.payload.len() > cursor+2 {
        loop {
            let number = &frame.payload[cursor];
            cursor += 1;
            let length = frame.payload[cursor] as usize;
            cursor += 1;

            if length == 0 {
                // Wildcard SSID.
                break;
            }

            if frame.payload.len() < cursor+length {
                trace!("Invalid tag length reported. Not calculating any more tagged parameters for this frame.");
                break;
            }

            let data = &frame.payload[cursor..cursor+length];
            cursor += length;

            if *number == 0 {
                let ssid_s = String::from_utf8_lossy(&data).to_string();
                if !ssid_s.trim().is_empty() {
                    ssid = Option::Some(ssid_s);
                }
            }

            if cursor >= frame.payload.len() {
                break;
            }
        }
    }

    Ok(Dot11ProbeRequestFrame {
        length: frame.length,
        header: frame.header.clone(),
        transmitter,
        ssid
    })
}