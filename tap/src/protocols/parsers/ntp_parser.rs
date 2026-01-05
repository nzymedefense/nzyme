use std::sync::Arc;
use chrono::{Duration, Utc};
use crate::tracemark;
use crate::wired::packets::{Datagram, NtpPacket, NtpPacketType, NtpTimestamp};

pub fn parse(udp: &Arc<Datagram>) -> Option<NtpPacket> {
    let payload = &udp.payload;

    // NTP header is at least 48 bytes long.
    if payload.len() < 48 {
        tracemark!("Payload too short.");
        return None;
    }

    let b0 = payload[0];

    // Parse flags.
    let leap_indicator = (b0 >> 6) & 0b11;
    let version = (b0 >> 3) & 0b111;
    let mode = b0 & 0b111;

    // Check version. It's almost always version 3 or 4 in real networks.
    if version < 3 || version > 4 {
        tracemark!("Invalid version: <{}>", version);
        return None;
    }

    // Check mode. We only allow the common modes 3 (client), 4 (server) or 5 (broadcast).
    let ntp_type = match mode {
        3 => NtpPacketType::Client,
        4 => NtpPacketType::Server,
        5 => NtpPacketType::Broadcast,
        _ => {
            tracemark!("Invalid mode: [{}]", mode);
            return None;
        }
    };

    // Check leap indicator.
    if leap_indicator > 3 {
        tracemark!("Invalid leap indicator: [{}]", leap_indicator);
        return None;
    }

    // Check stratum.
    let stratum = payload[1];
    if stratum > 16 {
        tracemark!("Invalid stratum {}", stratum);
        return None;
    }

    // Parse poll and precision.
    let poll = payload[2] as i8;
    let precision = payload[3] as i8;

    // Poll is log2(seconds).
    if poll < -2 || poll > 20 {
        tracemark!("Implausible poll: <{}>", poll);
        return None;
    }

    // Precision is log2(seconds) and usually negative.
    if precision > 0 || precision < -40 {
        tracemark!("Implausible precision: <{}>", precision);
        return None;
    }

    // Root delay and dispersion sanity.
    let root_delay_raw = i32::from_be_bytes(payload[4..8].try_into().unwrap());
    let root_dispersion_raw = u32::from_be_bytes(payload[8..12].try_into().unwrap());

    // Reject values that are way too large.
    if root_delay_raw.abs() > (10_000 << 16) {
        tracemark!("Root delay out of range.");
        return None;
    }
    if root_dispersion_raw > (10_000 << 16) {
        tracemark!("Root dispersion out of range.");
        return None;
    }

    // At least one of the major timestamps should be non-zero
    let ts_nonzero = payload[24..48]
        .chunks_exact(8)
        .any(|ts| ts.iter().any(|&b| b != 0));

    if !ts_nonzero {
        tracemark!("All timestamps zero.");
        return None;
    }

    // Transmit timestamp.
    let tx_secs =
        u32::from_be_bytes(payload[40..44].try_into().unwrap());

    // The timestamp MUST be set in server replies.
    if mode == 4 && tx_secs == 0 {
        tracemark!("Server packet with zero transmit timestamp.");
        return None;
    }

    // The timestamp SHOULD be set in client requests.
    if tx_secs != 0 {
        // Convert NTP seconds (since 1900) to UNIX seconds.
        let unix = tx_secs as i64 - 2_208_988_800;

        let now = Utc::now().timestamp();
        let window = Duration::days(10).num_seconds();

        if (unix - now).abs() > window {
            tracemark!("Transmit timestamp outside allowed window (tx=[{}], now=[{}])", unix, now);
            return None;
        }
    }

    // This is most likely a NTP packet. Parse.
    let root_delay_seconds = fixed_16_16_to_f64(root_delay_raw);
    let root_dispersion_seconds = fixed_16_16_to_f64(root_dispersion_raw as i32);
    let reference_id = u32::from_be_bytes(payload[12..16].try_into().unwrap());

    let reference_ts = read_ntp_timestamp(&payload[16..24]);
    let origin_ts = read_ntp_timestamp(&payload[24..32]);
    let receive_ts = read_ntp_timestamp(&payload[32..40]);
    let transmit_ts = read_ntp_timestamp(&payload[40..48]);

    let reference_timestamp = (!reference_ts.is_zero()).then_some(reference_ts);
    let origin_timestamp = (!origin_ts.is_zero()).then_some(origin_ts);
    let receive_timestamp = (!receive_ts.is_zero()).then_some(receive_ts);
    let transmit_timestamp = (!transmit_ts.is_zero()).then_some(transmit_ts);

    let transmit_time_utc = transmit_timestamp.and_then(|ts| ts.to_datetime_utc());

    Some(NtpPacket {
        source_mac: udp.source_mac.clone(),
        destination_mac: udp.destination_mac.clone(),
        source_address: udp.source_address,
        destination_address: udp.destination_address,
        source_port: udp.source_port,
        destination_port: udp.destination_port,
        size: payload.len() as u32,
        timestamp: udp.timestamp,
        ntp_type,
        leap_indicator,
        version,
        mode,
        stratum,
        poll,
        precision,
        root_delay_seconds,
        root_dispersion_seconds,
        reference_id,
        reference_timestamp,
        origin_timestamp,
        receive_timestamp,
        transmit_timestamp,
        transmit_time_utc,
    })
}

fn read_ntp_timestamp(b: &[u8]) -> NtpTimestamp {
    let seconds = u32::from_be_bytes(b[0..4].try_into().unwrap());
    let fraction = u32::from_be_bytes(b[4..8].try_into().unwrap());
    NtpTimestamp { seconds, fraction }
}

fn fixed_16_16_to_f64(v: i32) -> f64 {
    (v as f64) / 65536.0
}