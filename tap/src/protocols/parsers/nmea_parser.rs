use std::sync::Arc;
use anyhow::{bail, Error};
use chrono::{DateTime, NaiveTime, Utc};
use log::warn;
use crate::wireless::positioning::gnss::gnss_constellation::GNSSConstellation::GPS;
use crate::wireless::positioning::nmea::nmea_message::NMEAMessage;
use crate::wireless::positioning::nmea::nmea_sentences::{
    FixType, GPGGASentence, GPGSASentence, GPGSVSentence, SatelliteInfo
};

pub fn parse_gpgga(message: &Arc<NMEAMessage>) -> Result<GPGGASentence, Error> {
    let body = validate_nmea_checksum(&message.sentence)?;

    // Split fields.
    let fields: Vec<&str> = body.split(',').collect();
    if fields.len() < 12 {
        bail!("Incomplete GPGGA sentence: {}", message.sentence);
    }

    // Time.
    let t = fields[1];
    let time = if t.len() < 6 {
        None
    } else {
        let hh: u32 = t[0..2].parse()?;
        let mm: u32 = t[2..4].parse()?;
        let ss: u32 = t[4..6].parse()?;
        let nano = if let Some(dot) = t.find('.') {
            let frac = &t[dot + 1..];
            let f = format!("{frac:0<9}");
            f.parse().unwrap_or(0)
        } else {
            0
        };
        let time_part = NaiveTime::from_hms_nano_opt(hh, mm, ss, nano)
            .ok_or_else(|| anyhow::anyhow!("Invalid time components"))?;

        let today = Utc::now().date_naive();
        Some(DateTime::<Utc>::from_naive_utc_and_offset(
            today.and_time(time_part), Utc
        ))
    };

    // Latitude / Longitude.
    let (latitude, longitude) = if !fields[2].is_empty() && !fields[4].is_empty() {
        (Some(parse_coord(fields[2], fields[3].chars().next().unwrap())?),
         Some(parse_coord(fields[4], fields[5].chars().next().unwrap())?))
    } else {
        (None, None)
    };

    // Number of satellites.
    let num_satellites = fields[7].parse::<u8>().ok();

    // Altitude.
    let altitude_m = fields[9].parse::<f32>().ok();

    // Geoid separation.
    let geoid_separation_m = fields[11].parse::<f32>().ok();

    Ok(GPGGASentence {
        constellation: GPS,
        timestamp: message.timestamp,
        time,
        latitude,
        longitude,
        num_satellites,
        altitude_m,
        geoid_separation_m,
    })
}

pub fn parse_gpgsa(message: &Arc<NMEAMessage>) -> Result<GPGSASentence, Error> {
    let body = validate_nmea_checksum(&message.sentence)?;

    // Split fields.
    let fields: Vec<&str> = body.split(',').collect();
    if fields.len() < 18 {
        bail!("Incomplete GPGSA sentence: {}", message.sentence);
    }

    // Fix type.
    let fix = match fields[2].chars().next() {
        Some('1') => FixType::NoFix,
        Some('2') => FixType::Fix2D,
        Some('3') => FixType::Fix3D,
        _   => FixType::NoFix
    };

    // Satellite PRNs.
    let mut fix_satellites = Vec::new();
    for prn_str in &fields[3..15] {
        if !prn_str.is_empty() {
            match prn_str.parse::<u8>() {
                Ok(prn) => fix_satellites.push(prn),
                Err(_) => {
                    warn!("Invalid PRN: {}", message.sentence);
                    continue;
                }
            }
        }
    }

    // DOP values.
    let pdop = match fields[15] {
        "99.99" => None,
        val => val.parse::<f32>().ok(),
    };
    let hdop = match fields[16] {
        "99.99" => None,
        val => val.parse::<f32>().ok(),
    };
    let vdop = match fields[17] {
        "99.99" => None,
        val => val.parse::<f32>().ok(),
    };

    Ok(GPGSASentence {
        constellation: GPS,
        fix,
        fix_satellites,
        pdop,
        hdop,
        vdop,
    })
}

pub fn parse_gpgsv(message: &Arc<NMEAMessage>) -> Result<GPGSVSentence, Error> {
    let body = validate_nmea_checksum(&message.sentence)?;

    // Split fields.
    let fields: Vec<&str> = body.split(',').collect();
    if fields.len() < 8 {
        bail!("Incomplete GPGSV sentence: {}", message.sentence);
    }

    // Header counts.
    let total_messages = fields[1].parse::<u8>().ok();
    let message_number = fields[2].parse::<u8>().ok();
    let satellites_in_view = fields[3].parse::<u8>().ok();

    // Parse groups.
    let mut satellites = Vec::new();
    for chunk in fields[4..].chunks(4) {
        if chunk.len() < 4 {
            break;
        }

        let prn = chunk[0].parse::<u8>().ok();

        if prn.is_none() {
            continue;
        }

        let elevation_degrees = chunk[1].parse::<u8>().ok();
        let azimuth_degrees = chunk[2].parse::<u16>().ok();

        let snr_db = if chunk[3].is_empty() {
            None
        } else {
            chunk[3].parse::<u8>().ok()
        };

        satellites.push(SatelliteInfo {
            prn: prn.unwrap(),
            elevation_degrees,
            azimuth_degrees,
            snr_db,
        });
    }

    Ok(GPGSVSentence {
        constellation: GPS,
        total_messages,
        message_number,
        satellites_in_view,
        satellites,
    })
}

pub fn validate_nmea_checksum(sentence: &str) -> Result<&str, Error> {
    // Must start with ‘$’.
    if !sentence.starts_with('$') {
        bail!("Sentence must start with '$'");
    }

    // Checksum.
    let mut parts = sentence[1..].split('*');
    let body = parts
        .next()
        .ok_or_else(|| anyhow::anyhow!("Missing ‘*’ separator"))?;
    let checksum_str = parts
        .next()
        .ok_or_else(|| anyhow::anyhow!("Missing checksum field"))?;
    if parts.next().is_some() {
        bail!("Sentence must contain exactly one '*'");
    }

    let calculated = body.bytes().fold(0u8, |acc, b| acc ^ b);
    let given = u8::from_str_radix(checksum_str, 16)
        .map_err(|_| anyhow::anyhow!("Invalid checksum format: {}", checksum_str))?;

    // Compare.
    if calculated != given {
        bail!("Checksum mismatch: calculated {:02X}, found {:02X}", calculated, given);
    }

    Ok(body)
}

fn parse_coord(raw: &str, hemi: char) -> Result<f64, Error> {
    // Find the dot.
    let dot = raw
        .find('.')
        .ok_or_else(|| anyhow::anyhow!("Bad coord format: {}", raw))?;

    // We need at least "ddmm" before the dot.
    if dot < 2 {
        bail!("Invalid coord (too short before decimal): {}", raw);
    }

    // Number of degree‐digits is always `dot - 2`.
    let deg_len = dot - 2;
    let (d_str, m_str) = raw.split_at(deg_len);

    // Parse degrees and minutes.
    let deg: f64 = d_str
        .parse()
        .map_err(|_| anyhow::anyhow!("Invalid degrees: {}", d_str))?;
    let min: f64 = m_str
        .parse()
        .map_err(|_| anyhow::anyhow!("Invalid minutes: {}", m_str))?;

    // Combine, then apply hemisphere.
    let mut val = deg + (min / 60.0);
    match hemi {
        'N' | 'E' => {}
        'S' | 'W' => val = -val,
        c => bail!("Unknown hemisphere indicator: {}", c),
    }

    Ok(val)
}
