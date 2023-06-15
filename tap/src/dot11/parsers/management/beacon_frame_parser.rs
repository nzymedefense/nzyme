use std::sync::Arc;

use anyhow::{Error, bail};
use bitvec::{view::BitView, order::Lsb0};
use byteorder::{LittleEndian, ByteOrder, BigEndian};
use log::{trace, info};
use sha2::{Sha256, Digest};

use crate::{dot11::frames::{Dot11Frame, Dot11BeaconFrame, BeaconCapabilities, InfraStructureType, BeaconTaggedParameters, CountryInformation, RegulatoryEnvironment}, helpers::network::to_mac_address_string};

pub fn parse(frame: &Arc<Dot11Frame>) -> Result<Dot11BeaconFrame, Error> {

    if frame.payload.len() < 12 {
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
    let mut ssid: Option<String> = Option::None;
    let mut supported_rates: Option<Vec<String>> = Option::None;
    let mut extended_supported_rates: Option<Vec<String>> = Option::None;
    let mut country_information: Option<CountryInformation> = Option::None;
    let mut ht_capabilities: Option<Vec<u8>> = Option::None;
    let mut extended_capabilities: Option<Vec<u8>> = Option::None;

    // WPS.
    let mut has_wps = false;

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
                    let ssid_s = String::from_utf8_lossy(&data).to_string();
                    if !ssid_s.trim().is_empty() {
                        ssid = Option::Some(ssid_s);
                    }
                },
                1 => {
                    // Supported rates.
                    supported_rates = Option::Some(parse_supported_rates(&data));
                }
                7 => {
                    // Country information.
                    match parse_country_information(&data) {
                        Ok(ci) => {
                            country_information = Option::Some(ci);
                        },
                        Err(e) => trace!("Could not parse country information: {}", e)
                    }
                }
                45 => {
                    // HT capabilities.
                    ht_capabilities = Option::Some(data.to_vec());
                },
                48 => {
                    // WPA.
                }
                50 => {
                    // Extended supported rates.
                    extended_supported_rates = Option::Some(parse_extended_supported_rates(&data));
                },
                127 => {
                    // Extended capabilities.
                    extended_capabilities = Option::Some(data.to_vec());
                }
                221 => {
                    // Vendor tags.
                    if data.len() < 4 {
                        trace!("Vendor tag too short to hold even OUI tag. Skipping.");
                        continue;
                    }

                    match &data[0..3] {
                        // Microsoft Corp.
                        [0x00,0x50,0xf2] => {
                            match &data[4] {
                                // WPS.
                                0x04 => has_wps = true,
                                // WPA.
                                0x01 => todo!("WPA parsing."),
                                _ => {}
                            }
                        },
                        _ => {}
                    }
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

    let tagged_parameters = BeaconTaggedParameters {
        ssid: ssid.clone(),
        supported_rates,
        extended_supported_rates,
        country_information,
        ht_capabilities,
        extended_capabilities
    };

    let fingerprint = calculate_fingerprint(&capabilities, &tagged_parameters);

    Ok(Dot11BeaconFrame{
        destination,
        transmitter,
        timestamp,
        interval,
        capabilities,
        ssid,
        tagged_parameters,
        fingerprint,
        encryption: Option::None,
        has_wps
    })
}

fn parse_country_information(data: &[u8]) -> Result<CountryInformation, Error> {
    if !data.len() != 6 {
        bail!("Country information must be 8 bytes, provided <{}>.", data.len());
    }
 
    let country_code = String::from_utf8_lossy(&data[0..2]).to_string();

    let environment = match &data[2] {
        32 => RegulatoryEnvironment::All,
        73 => RegulatoryEnvironment::Indoors,
        79 => RegulatoryEnvironment::Outdoors,
        _ => RegulatoryEnvironment::Unknown
    };

    let first_channel = *&data[3];
    let channel_count = *&data[4];
    let max_transmit_power = *&data[5];

    Ok(CountryInformation {
        country_code,
        environment,
        first_channel,
        channel_count,
        max_transmit_power,
    })
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

pub fn calculate_fingerprint(caps: &BeaconCapabilities, tagged_params: &BeaconTaggedParameters) -> String {
    let mut factors: Vec<u8> = Vec::new();

    // Infrastructure type.
    match caps.infrastructure_type {
        InfraStructureType::Invalid => factors.push(0),
        InfraStructureType::AccessPoint => factors.push(1),
        InfraStructureType::AdHoc => factors.push(2),
    }

    // Additional capabitilies information.
    factors.push(caps.secured as u8);
    factors.push(caps.short_preamble as u8);
    factors.push(caps.pbcc as u8);
    factors.push(caps.channel_agility as u8);
    factors.push(caps.short_slot_time as u8);
    factors.push(caps.dsss_ofdm as u8);

    // Supported rates.
    if tagged_params.supported_rates.is_some() {
        for rate in tagged_params.supported_rates.as_ref().unwrap() {
            factors.extend(rate.as_bytes());
        }
    }
    
    // Extended spported rates.
    if tagged_params.extended_supported_rates.is_some() {
        for rate in tagged_params.extended_supported_rates.as_ref().unwrap() {
            factors.extend(rate.as_bytes());
        }
    }

    // HT capabilities.
    if tagged_params.ht_capabilities.is_some() {
        factors.extend(tagged_params.ht_capabilities.as_ref().unwrap());
    }

    // Extended capabilities.
    if tagged_params.extended_capabilities.is_some() {
        factors.extend(tagged_params.extended_capabilities.as_ref().unwrap());
    }

    // WPS

    let hash = Sha256::digest(factors);

    format!("{:2x}", hash)
}