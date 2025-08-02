use std::sync::Arc;
use anyhow::{bail, Error};
use chrono::{DateTime, NaiveTime, Utc};
use crate::wireless::positioning::nmea::nmea_message::NMEAMessage;
use crate::wireless::positioning::nmea::nmea_sentences::{FixQuality, FixType, GPGGASentence, GPGSASentence, GPGSVSentence, SatelliteInfo, SelectionMode};

pub fn parse_gpgga(message: &Arc<NMEAMessage>) -> Result<GPGGASentence, Error> {
    let body = validate_nmea_checksum(&message.sentence)?;

    // Split fields.
    let fields: Vec<&str> = body.split(',').collect();
    if fields.len() < 12 {
        bail!("Incomplete GPGGA sentence: {}", message.sentence);
    }

    // Time.
    let t = fields[1];
    if t.len() < 6 {
        bail!("Invalid time format: {}", t);
    }
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
    let time: DateTime<Utc> = DateTime::<Utc>::from_naive_utc_and_offset(
        today.and_time(time_part), Utc
    );

    // Latitude / Longitude.
    let latitude  = parse_coord(fields[2], fields[3].chars().next().unwrap())?;
    let longitude = parse_coord(fields[4], fields[5].chars().next().unwrap())?;

    // Fix quality.
    let fq = fields[6].parse::<u8>()
        .map_err(|_| anyhow::anyhow!("Invalid fix quality: {}", fields[6]))?;
    let fix_quality = match fq {
        0 => FixQuality::Invalid,
        1 => FixQuality::GPS,
        2 => FixQuality::DGPS,
        3 => FixQuality::PPS,
        4 => FixQuality::RTK,
        5 => FixQuality::FloatRTK,
        6 => FixQuality::Estimated,
        7 => FixQuality::ManualInput,
        8 => FixQuality::Simulation,
        other => bail!("Invalid fix quality: {}", message.sentence),
    };

    // Number of satellites.
    let num_satellites = fields[7]
        .parse::<u8>()
        .map_err(|_| anyhow::anyhow!("Invalid satellite count: {}", fields[7]))?;

    // HDOP.
    let hdop = fields[8]
        .parse::<f32>()
        .map_err(|_| anyhow::anyhow!("Invalid HDOP: {}", fields[8]))?;

    // Altitude.
    let altitude_m = fields[9]
        .parse::<f32>()
        .map_err(|_| anyhow::anyhow!("Invalid altitude: {}", fields[9]))?;

    // Geoid separation.
    let geoid_separation_m = fields[11]
        .parse::<f32>()
        .map_err(|_| anyhow::anyhow!("Invalid geoid separation: {}", fields[11]))?;

    Ok(GPGGASentence {
        constellation: message.constellation.clone(),
        time,
        latitude,
        longitude,
        fix_quality,
        num_satellites,
        hdop,
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

    // Selection mode.
    let mode = match fields[1].chars().next() {
        Some('A') => SelectionMode::Automatic,
        Some('M') => SelectionMode::Manual,
        Some(c)   => bail!("Unknown selection mode: {}", message.sentence),
        None      => bail!("Missing selection mode field"),
    };

    // Fix type.
    let fix = match fields[2].chars().next() {
        Some('1') => FixType::NoFix,
        Some('2') => FixType::Fix2D,
        Some('3') => FixType::Fix3D,
        Some(c)   => bail!("Unknown fix type: {}", message.sentence),
        None      => bail!("Missing fix type field"),
    };

    // Satellite PRNs.
    let mut fix_satellites = Vec::new();
    for prn_str in &fields[3..15] {
        if !prn_str.is_empty() {
            let prn = prn_str.parse::<u8>()
                .map_err(|_| anyhow::anyhow!("Invalid satellite PRN: {}", message.sentence))?;
            fix_satellites.push(prn);
        }
    }

    // DOP values.
    let pdop = fields[15].parse::<f32>()
        .map_err(|_| anyhow::anyhow!("Invalid PDOP value: {}", message.sentence))?;
    let hdop = fields[16].parse::<f32>()
        .map_err(|_| anyhow::anyhow!("Invalid HDOP value: {}", message.sentence))?;
    let vdop = fields[17].parse::<f32>()
        .map_err(|_| anyhow::anyhow!("Invalid VDOP value: {}", message.sentence))?;

    Ok(GPGSASentence {
        constellation: message.constellation.clone(),
        mode,
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
    let total_messages = fields[1]
        .parse::<u8>()
        .map_err(|_| anyhow::anyhow!("Invalid total message count: {}", message.sentence))?;
    let message_number = fields[2]
        .parse::<u8>()
        .map_err(|_| anyhow::anyhow!("Invalid message number: {}", message.sentence))?;
    let satellites_in_view = fields[3]
        .parse::<u8>()
        .map_err(|_| anyhow::anyhow!("Invalid satellites-in-view: {}", message.sentence))?;

    // Parse groups.
    let mut satellites = Vec::new();
    for chunk in fields[4..].chunks(4) {
        if chunk.len() < 4 {
            break;
        }
        let prn = chunk[0]
            .parse::<u8>()
            .map_err(|_| anyhow::anyhow!("Invalid PRN: {}", message.sentence))?;
        let elevation_degrees = chunk[1]
            .parse::<u8>()
            .map_err(|_| anyhow::anyhow!("Invalid elevation: {}", message.sentence))?;
        let azimuth_degrees = chunk[2]
            .parse::<u16>()
            .map_err(|_| anyhow::anyhow!("Invalid azimuth: {}", message.sentence))?;
        let snr_db = if chunk[3].is_empty() {
            None
        } else {
            Some(chunk[3].parse::<u8>()
                     .map_err(|_| anyhow::anyhow!("Invalid SNR: {}", message.sentence))?, )
        };

        satellites.push(SatelliteInfo {
            prn,
            elevation_degrees,
            azimuth_degrees,
            snr_db,
        });
    }

    Ok(GPGSVSentence {
        constellation: message.constellation.clone(),
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
    let dot = raw.find('.').ok_or_else(|| anyhow::anyhow!("Bad coord format: {}", raw))?;
    let deg_len = if raw.len() - dot > 5 { 3 } else { 2 };
    let (d, m) = raw.split_at(deg_len);
    let deg: f64 = d.parse().map_err(|_| anyhow::anyhow!("Invalid degrees: {}", d))?;
    let min: f64 = m.parse().map_err(|_| anyhow::anyhow!("Invalid minutes: {}", m))?;
    let mut val = deg + (min / 60.0);
    match hemi {
        'N' | 'E' => {}
        'S' | 'W' => val = -val,
        c => bail!("Unknown hemisphere: {}", c),
    }
    Ok(val)
}