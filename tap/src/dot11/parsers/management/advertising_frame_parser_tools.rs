use anyhow::{bail, Error};
use crate::dot11::frames::{CipherSuite, CipherSuites, CountryInformation, Dot11Capabilities, EncryptionProtocol, InfraStructureType, KeyManagementMode, RegulatoryEnvironment, SecurityInformation, TaggedParameters};
use bitvec::{view::BitView, order::Lsb0};
use byteorder::{ByteOrder, LittleEndian};
use log::trace;
use sha2::{Digest, Sha256};

pub fn parse_capabilities(mask: &[u8]) -> Result<Dot11Capabilities, Error> {
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

    Ok(Dot11Capabilities {
        infrastructure_type,
        privacy: *bmask.get(4).unwrap(),
        short_preamble: *bmask.get(5).unwrap(),
        pbcc: *bmask.get(6).unwrap(),
        channel_agility: *bmask.get(7).unwrap(),
        short_slot_time: *bmask.get(10).unwrap(),
        dsss_ofdm: *bmask.get(13).unwrap(),
    })
}

pub struct TaggedParameterParserData {
    pub tagged_parameters: TaggedParameters,
    pub security: Vec<SecurityInformation>,
    pub security_bytes: Vec<u8>,
    pub has_wps: bool
}

pub fn parse_tagged_parameters(payload: &[u8]) -> Result<TaggedParameterParserData, Error> {
    let mut ssid: Option<String> = Option::None;
    let mut supported_rates: Option<Vec<f32>> = Option::None;
    let mut extended_supported_rates: Option<Vec<f32>> = Option::None;
    let mut country_information: Option<CountryInformation> = Option::None;
    let mut ht_capabilities: Option<Vec<u8>> = Option::None;
    let mut extended_capabilities: Option<Vec<u8>> = Option::None;
    let mut security: Vec<SecurityInformation> = Vec::new();
    let mut security_bytes: Vec<u8> = Vec::new(); // Raw bytes for quick fingerprint calculation.

    // WPS.
    let mut has_wps = false;

    let mut cursor: usize = 36;
    if payload.len() > cursor+2 {
        loop {
            if cursor >= payload.len() {
                break;
            }

            let number = &payload[cursor];
            cursor += 1;
            let length = payload[cursor] as usize;
            cursor += 1;

            if payload.len() < cursor+length {
                trace!("Invalid tag length reported. Not calculating any more tagged parameters for this frame.");
                break;
            }

            let data = &payload[cursor..cursor+length];
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
                    // RSN. (WPA 2/3)
                    let suites: CipherSuites = match parse_wpa_security(&data) {
                        Ok(suites) => suites,
                        Err(e) => {
                            trace!("Could not parse RSN information: {}", e);
                            continue;
                        }
                    };

                    let protocols: Vec<EncryptionProtocol>;
                    if suites.key_management_modes.contains(&KeyManagementMode::SAE)
                        || suites.key_management_modes.contains(&KeyManagementMode::FTSAE) {
                        protocols = vec![EncryptionProtocol::WPA2, EncryptionProtocol::WPA3];
                    } else {
                        protocols = vec![EncryptionProtocol::WPA2];
                    }

                    security.push(SecurityInformation {protocols, suites: Option::Some(suites)});
                    security_bytes.extend(data);
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
                            match &data[3] {
                                // WPS.
                                0x04 => has_wps = true,

                                // WPA 1.
                                0x01 => {
                                    let suites: CipherSuites = match parse_wpa_security(&data[4..data.len()]) {
                                        Ok(suites) => suites,
                                        Err(e) => {
                                            trace!("Could not parse WPA1 information: {}", e);
                                            continue;
                                        }
                                    };

                                    security.push(SecurityInformation {
                                        protocols: vec![EncryptionProtocol::WPA1],
                                        suites: Option::Some(suites)
                                    });
                                    security_bytes.extend(data);
                                }

                                _ => { /* Ignore other types. */ }
                            }
                        },
                        _ => {}
                    }
                }
                _ => {}
            };
        }
    } else {
        trace!("Tagged parameters are too short. Not calculating for this frame.");
    }

    let tagged_parameters = TaggedParameters {
        ssid: ssid.clone(),
        supported_rates,
        extended_supported_rates,
        country_information,
        ht_capabilities,
        extended_capabilities
    };

    Ok(TaggedParameterParserData {
        tagged_parameters,
        security,
        security_bytes,
        has_wps,
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


fn parse_supported_rates(data: &[u8]) -> Vec<f32> {
    let mut rates: Vec<f32> = Vec::new();

    for rate in data {
        match rate ^ 0b1000_0000 {
            2   => rates.push(1.0),
            4   => rates.push(2.0),
            11  => rates.push(5.5),
            12  => rates.push(6.0),
            18  => rates.push(9.0),
            22  => rates.push(11.0),
            24  => rates.push(12.0),
            36  => rates.push(18.0),
            44  => rates.push(22.0),
            48  => rates.push(24.0),
            66  => rates.push(33.0),
            72  => rates.push(36.0),
            96  => rates.push(48.0),
            108 => rates.push(54.0),
            _   => trace!("Invalid supported rate <{}>", rate)
        }
    }

    rates
}

fn parse_extended_supported_rates(data: &[u8]) -> Vec<f32> {
    let mut rates: Vec<f32> = Vec::new();

    for rate in data {
        rates.push((rate >> 1) as f32);
    }

    rates
}

fn parse_wpa_security(data: &[u8]) -> Result<CipherSuites, Error> {
    if LittleEndian::read_u16(&data[0..2]) != 1 {
        bail!("Unsupported RSN/WPA version <{:?}>.", &data)
    }

    if data.len() < 5 {
        bail!{"RSN/WPA info doesn't fit group cipher suite."}
    }

    // Group cipher.
    let group_cipher = match parse_cipher_suite(&data[2..6]) {
        Ok(cs) => cs,
        Err(e) => bail!("Could not parse group cipher suite: {}", e)
    };

    if data.len() < 8 {
        bail!{"RSN doesn't fit pairwise cipher suite count."};
    }

    let mut pairwise_ciphers: Vec<CipherSuite> = Vec::new();
    let pair_suite_count = LittleEndian::read_u16(&data[6..8]) as usize;
    let mut cursor: usize = 8;
    for _ in 0..pair_suite_count {
        match parse_cipher_suite(&data[cursor..cursor+4]) {
            Ok(cipher) => pairwise_ciphers.push(cipher),
            Err(e) => bail!("Could not parse pairwise cipher: {}", e)
        }
        cursor += 4;
    }

    if data.len() < cursor+3 {
        bail!{"RSN doesn't fit key management suite count."};
    }


    let mut key_management_modes: Vec<KeyManagementMode> = Vec::new();
    let key_management_suite_count = LittleEndian::read_u16(&data[cursor..cursor+2]) as usize;
    cursor+=2;
    for _ in 0..key_management_suite_count {
        match parse_key_management_suite(&data[cursor..cursor+4]) {
            Ok(cipher) => key_management_modes.push(cipher),
            Err(e) => bail!("Could not parse key management cipher: {}", e)
        }
        cursor += 4;
    }

    Ok (CipherSuites {
        group_cipher,
        pairwise_ciphers,
        key_management_modes
    })
}

fn parse_key_management_suite(data: &[u8]) -> Result<KeyManagementMode, Error> {
    if data.len() != 4 {
        bail!("Invalid cipher suite length: <{}>.", data.len())
    }

    if data[0..3] != [0x00, 0x0f, 0xac] && data[0..3] != [0x00, 0x50, 0xf2] {
        bail!("Invalid cipher suite OUI: <{:02x?}>.", data)
    }

    match data[3] {
        0x01 => Ok(KeyManagementMode::X802_1),
        0x02 => Ok(KeyManagementMode::PSK),
        0x03 => Ok(KeyManagementMode::FT802_1X),
        0x04 => Ok(KeyManagementMode::FTPSK),
        0x08 => Ok(KeyManagementMode::SAE),
        0x09 => Ok(KeyManagementMode::FTSAE),
        _ => Ok(KeyManagementMode::Unknown)
    }
}

fn parse_cipher_suite(data: &[u8]) -> Result<CipherSuite, Error> {
    if data.len() != 4 {
        bail!("Invalid cipher suite length: <{}>.", data.len())
    }

    if data[0..3] != [0x00, 0x0f, 0xac] && data[0..3] != [0x00, 0x50, 0xf2] {
        bail!("Invalid cipher suite OUI: <{:02x?}>.", data)
    }

    match data[3] {
        0x00 => Ok(CipherSuite::None),
        0x01 => Ok(CipherSuite::WEP),
        0x02 => Ok(CipherSuite::TKIP),
        0x04 => Ok(CipherSuite::CCMP),
        0x05 => Ok(CipherSuite::WEP104),
        0x06 => Ok(CipherSuite::BIPCMAC128),
        0x08 => Ok(CipherSuite::GCMP128),
        0x09 => Ok(CipherSuite::GCMP256),
        0x0a => Ok(CipherSuite::CCMP256),
        0x0b => Ok(CipherSuite::BIPGMAC128),
        0x0c => Ok(CipherSuite::BIPGMAC256),
        0x0d => Ok(CipherSuite::BIPCMAC256),
        _ => Ok(CipherSuite::Unknown)
    }
}

pub fn calculate_fingerprint(caps: &Dot11Capabilities,
                             tagged_params: &TaggedParameters,
                             has_wps: &bool,
                             security: &Vec<u8>) -> String {
    let mut factors: Vec<u8> = Vec::new();

    // Infrastructure type.
    match caps.infrastructure_type {
        InfraStructureType::Invalid => factors.push(0),
        InfraStructureType::AccessPoint => factors.push(1),
        InfraStructureType::AdHoc => factors.push(2),
    }

    // Additional capabitilies information.
    factors.push(caps.privacy as u8);
    factors.push(caps.short_preamble as u8);
    factors.push(caps.pbcc as u8);
    factors.push(caps.channel_agility as u8);
    factors.push(caps.short_slot_time as u8);
    factors.push(caps.dsss_ofdm as u8);

    // Supported rates.
    if tagged_params.supported_rates.is_some() {
        for rate in tagged_params.supported_rates.as_ref().unwrap() {
            factors.extend(rate.to_le_bytes());
        }
    }

    // Extended spported rates.
    if tagged_params.extended_supported_rates.is_some() {
        for rate in tagged_params.extended_supported_rates.as_ref().unwrap() {
            factors.extend(rate.to_le_bytes());
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
    factors.push(*has_wps as u8);

    // Security / Encryption.
    factors.extend(security);

    let hash = Sha256::digest(factors);

    format!("{:2x}", hash)
}

pub fn decide_encryption_protocol(capabilities: &Dot11Capabilities,
                                  tagged_data: &mut TaggedParameterParserData) -> bool {
    if capabilities.privacy && tagged_data.security.is_empty() {
        tagged_data.security.push(
            SecurityInformation {
                protocols: vec![EncryptionProtocol::WEP],
                suites: Option::None
            }
        )
    };

    false
}