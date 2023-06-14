use std::{sync::Arc, io::Read};

use anyhow::{Error, bail};
use bitvec::{view::BitView, order::Lsb0, store::BitStore, prelude::Msb0};
use byteorder::{LittleEndian, ByteOrder, ReadBytesExt};
use log::{info, trace};
use serde::__private::from_utf8_lossy;

use crate::{dot11::frames::{Dot11Frame, Dot11BeaconFrame, BeaconCapabilities, InfraStructureType, BeaconTaggedParameters}, helpers::network::to_mac_address_string};

pub fn parse(frame: &Arc<Dot11Frame>) { //-> Result<Dot11BeaconFrame, Error> {

    if frame.payload.len() < 12 {
        trace!("Beacon frame payload too short to hold fixed parameters. Discarding.");
        return
    }

    // MAC header.
    let destination = to_mac_address_string(&frame.payload[4..10]);
    let transmitter = to_mac_address_string(&frame.payload[10..16]);

    // Fixed capabilities.
    let timestamp = LittleEndian::read_u64(&frame.payload[24..32]);
    let interval = LittleEndian::read_u16(&frame.payload[32..34]);
    let capabilities = parse_capabilities(&frame.payload[34..36]);

    // Tagged parameters.
    let mut ssid: Option<String> = Option::None;
    let mut supportedRates: Option<Vec<String>> = Option::None;
    let mut extendedSupportedRates: Option<Vec<String>> = Option::None;
    let mut cursor: usize = 36;
    if frame.payload.len() > 36+2 {
        loop {
            let number = &frame.payload[cursor];
            cursor += 1;
            let length = frame.payload[cursor] as usize;
            cursor += 1;

            if frame.payload.len() < cursor+length {
                trace!("Invalid tag length reported. Not calculating any more tagged parameters for this frame.");
                break;
            }

            let data = &frame.payload[cursor..cursor+length];
            cursor += length;

            match number {
                0 => {
                    // SSID.
                    let ssid_s = from_utf8_lossy(data).to_string();
                    if !ssid_s.trim().is_empty() {
                        ssid = Option::Some(ssid_s);
                    }
                },
                1 => {
                    // Supported rates.
                    supportedRates = Option::Some(parse_supported_rates(data));
                }
                7 => {
                    todo!()
                }
                50 => {
                    // Extended supported Rates.
                    extendedSupportedRates = Option::Some(parse_extended_supported_rates(data));
                }
                _ => {}
            };


            if cursor >= frame.payload.len() {
                break;
            }
        }
    } else {
        trace!("Tagged parameters are too short. Not calculating for this frame.");
    }

    /*let tagged_params = BeaconTaggedParameters {
        ssid,
        supported_rates: todo!(),
        extended_supported_rates: todo!(),
        country_information: todo!(),
    }*/

    //info!("caps: {:?}", capabilities);

}

pub fn parse_capabilities(mask: &[u8]) -> Result<BeaconCapabilities, Error> {
    if mask.len() != 2 {
        bail!("Capabilities flags must be 2 bytes, provided <{}>.", mask.len())
    }

    let bmask = mask.view_bits::<Lsb0>();

    /*
     * We are using unwrap() here because the bounds length 
     * above ensures enough bits for the access ops.
     */

    let ess = *bmask.get(0).unwrap();
    let ibss = *bmask.get(1).unwrap();

    let infrastructure_type: InfraStructureType;
    if ess && !ibss {
        infrastructure_type = InfraStructureType::AccessPoint;
    } else if !ess && ibss {
        infrastructure_type = InfraStructureType::AdHoc;
    } else {
        infrastructure_type = InfraStructureType::Invalid;
        trace!("Invalid beacon infrastructure type.")
    }

    Ok(BeaconCapabilities {
        infrastructure_type,
        secured: *bmask.get(4).unwrap(),
        short_preamble: *bmask.get(5).unwrap(),
        pbcc: *bmask.get(6).unwrap(),
        channel_agility: *bmask.get(7).unwrap(),
        short_slot_time: *bmask.get(10).unwrap(),
        dsss_ofdm: *bmask.get(13).unwrap(),
    })
}

fn parse_supported_rates(data: &[u8]) -> Vec<String> {
    let mut rates: Vec<String> = Vec::new();

    for rate in data {
        match rate {
            2   => rates.push("1".to_string()),
            4   => rates.push("2".to_string()),
            11  => rates.push("5.5".to_string()),
            12  => rates.push("6".to_string()),
            18  => rates.push("9".to_string()),
            22  => rates.push("11".to_string()),
            24  => rates.push("12".to_string()),
            36  => rates.push("18".to_string()),
            44  => rates.push("22".to_string()),
            48  => rates.push("24".to_string()),
            66  => rates.push("33".to_string()),
            72  => rates.push("36".to_string()),
            96  => rates.push("48".to_string()),
            108 => rates.push("54".to_string()),
            _   => trace!("Invalid supported rate <{}>", rate)
        }
    }

    rates
}

fn parse_extended_supported_rates(data: &[u8]) -> Vec<String> {
    let mut rates: Vec<String> = Vec::new();

    for rate in data {
        rates.push((rate >> 1).to_string());
    }

    rates
}

pub fn calculate_fingerprint(caps: BeaconCapabilities, params: BeaconTaggedParameters) -> String {
    let mut factors: Vec<u8> = Vec::new();

    match caps.infrastructure_type {
        InfraStructureType::Invalid => factors.push(0),
        InfraStructureType::AccessPoint => factors.push(1),
        InfraStructureType::AdHoc => factors.push(2),
    }

    factors.push(caps.secured as u8);
    factors.push(caps.short_preamble as u8);
    factors.push(caps.pbcc as u8);
    factors.push(caps.channel_agility as u8);
    factors.push(caps.short_slot_time as u8);
    factors.push(caps.dsss_ofdm as u8);

    /*if params.supported_rates.is_some() {
        // TODO use a string appender logic here.
        factors.extend(&params.supported_rates.unwrap());
    }*/
    
    if params.country_information.is_some() {
        factors.extend(&params.country_information.unwrap());
    }

    /*
      TODO:
       1   // Supported Rates
       7   // Country Information
       45  // HT Capabilities
       50  // Extended Supported Rates
       127 // Extended Capabilities
     */

    "tbd".to_string()
}