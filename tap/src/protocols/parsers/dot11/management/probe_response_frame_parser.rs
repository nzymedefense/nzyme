use std::sync::{Arc, Mutex};
use anyhow::{bail, Error};
use byteorder::{ByteOrder, LittleEndian};
use crate::wireless::dot11::frames::{Dot11Frame, Dot11ProbeResponseFrame};
use crate::protocols::parsers::dot11::management::advertising_frame_parser_tools::{calculate_fingerprint, decide_encryption_protocol, parse_capabilities, parse_tagged_parameters};
use crate::helpers::network::to_mac_address_string;
use crate::messagebus::bus::Bus;
use crate::metrics::Metrics;

pub fn parse(frame: &Arc<Dot11Frame>,
             bus: Arc<Bus>,
             metrics: Arc<Mutex<Metrics>>) -> Result<Dot11ProbeResponseFrame, Error> {
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
    let mut tagged_data = match parse_tagged_parameters(
        &frame.payload, transmitter.clone(), bus, metrics) {
        Ok(tagged_data) => tagged_data,
        Err(e) => {
            bail!("Could not parse beacon tagged parameters. Skipping frame. Error: {}", e);
        }
    };

    // If there is no WPA1/2/3, but the privacy bit is set, WEP is in use.
    decide_encryption_protocol(&capabilities, &mut tagged_data);

    let fingerprint = calculate_fingerprint(
        &transmitter,
        &capabilities,
        &tagged_data.tagged_parameters,
        &tagged_data.has_wps,
        &tagged_data.security_bytes
    );

    Ok(Dot11ProbeResponseFrame{
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