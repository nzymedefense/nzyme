use std::sync::Arc;

use anyhow::{Error, bail};
use bitvec::{view::BitView, order::Lsb0};
use byteorder::{LittleEndian, ByteOrder};
use log::{info, trace};

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
                    info!("SSID! len: {}, val: {}", length, std::str::from_utf8(data).unwrap_or("[invalid-encoding]"))
                },
                _ => info!("tag#{} len: {}", number, length)
            };

            if cursor >= frame.payload.len() {
                break;
            }
        }
    } else {
        trace!("Tagged parameters are too short. Not calculating for this frame.");

    }

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

    if params.supported_rates.is_some() {
        // TODO use a string appender logic here.
        factors.extend(&params.supported_rates.unwrap());
    }
    
    if params.country_information.is_some() {
        factors.extend(&params.country_information.unwrap());
    }

    /*
    .add(1)   // Supported Rates
    .add(7)   // Country Information

    
    .add(45)  // HT Capabilities
    .add(50)  // Extended Supported Rates
    .add(127) // Extended Capabilities
     */

    "tbd".to_string()
}