use std::sync::Arc;

use anyhow::{Error, bail};

use byteorder::{LittleEndian, ByteOrder};

use crate::{dot11::frames::{Dot11Frame, Dot11BeaconFrame}, helpers::network::to_mac_address_string};
use crate::dot11::parsers::management::advertising_frame_parser_tools::{calculate_fingerprint, decide_encryption_protocol, parse_capabilities, parse_tagged_parameters};

pub fn parse(frame: &Arc<Dot11Frame>) -> Result<Dot11BeaconFrame, Error> {
    if frame.payload.len() < 37 {
        bail!("Beacon frame payload too short to hold fixed parameters. Discarding.");
    }

    // MAC header.
    let destination = to_mac_address_string(&frame.payload[4..10]);
    let transmitter = to_mac_address_string(&frame.payload[10..16]);

    // Fixed capabilities.
    let timestamp = LittleEndian::read_u64(&frame.payload[24..32]);
    let interval = LittleEndian::read_u16(&frame.payload[32..34]);
    let capabilities = match parse_capabilities(&frame.payload[34..36]) {
        Ok(caps) => caps,
        Err(e) => {
            bail!("Could not parse beacon capabilities. Skipping frame. Error: {}", e);
        }
    };

    // Tagged parameters.
    let mut tagged_data = match parse_tagged_parameters(&frame.payload) {
        Ok(tagged_data) => tagged_data,
        Err(e) => {
            bail!("Could not parse beacon tagged parameters. Skipping frame. Error: {}", e);
        }
    };

    // If there is no WPA1/2/3, but the privacy bit is set, WEP is in use.
    decide_encryption_protocol(&capabilities, &mut tagged_data);

    let fingerprint = calculate_fingerprint(
        &capabilities,
        &tagged_data.tagged_parameters,
        &tagged_data.has_wps,
        &tagged_data.security_bytes
    );

    Ok(Dot11BeaconFrame{
        receive_time: frame.receive_time,
        header: frame.header.clone(),
        length: frame.length,
        tagged_parameters: tagged_data.tagged_parameters,
        security: tagged_data.security,
        has_wps: tagged_data.has_wps,
        destination,
        transmitter,
        timestamp,
        interval,
        capabilities,
        fingerprint
    })
}