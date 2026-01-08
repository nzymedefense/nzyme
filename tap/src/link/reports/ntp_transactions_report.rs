use std::collections::{HashMap, HashSet};
use std::sync::MutexGuard;
use chrono::{DateTime, Duration, Utc};
use serde::Serialize;
use crate::state::tables::ntp_table::NtpTransaction;

#[derive(Serialize)]
pub struct NtpTransactionsReport {
    transactions: Vec<NtpTransactionReport>
}

#[derive(Serialize)]
pub struct NtpTransactionReport {
    pub complete: bool,
    pub notes: HashSet<String>,
    pub client_mac: Option<String>,
    pub server_mac: Option<String>,
    pub client_address: Option<String>,
    pub server_address: Option<String>,
    pub client_port: Option<u16>,
    pub server_port: Option<u16>,
    pub request_size: Option<u32>,
    pub response_size: Option<u32>,
    pub timestamp_client_transmit: Option<DateTime<Utc>>,
    pub timestamp_server_receive: Option<DateTime<Utc>>,
    pub timestamp_server_transmit: Option<DateTime<Utc>>,
    pub timestamp_client_tap_receive: Option<DateTime<Utc>>,
    pub timestamp_server_tap_receive: Option<DateTime<Utc>>,
    pub server_version: Option<u8>,
    pub client_version: Option<u8>,
    pub server_mode: Option<u8>,
    pub client_mode: Option<u8>,
    pub stratum: Option<u8>,
    pub leap_indicator: Option<u8>,
    pub precision: Option<i8>,
    pub poll_interval: Option<i8>,
    pub root_delay_seconds: Option<f64>,
    pub root_dispersion_seconds:Option<f64>,
    pub reference_id: Option<u32>,
    pub delay_seconds: Option<f64>,
    pub offset_seconds: Option<f64>,
    pub sensor_rtt_seconds: Option<f64>,
    pub server_processing_seconds: Option<f64>
}

pub fn generate(txs: &Vec<NtpTransaction>) -> NtpTransactionsReport {
    let mut transactions: Vec<NtpTransactionReport> = Vec::new();

    for tx in txs {
        let (
            delay_seconds,
            offset_seconds,
            sensor_rtt_seconds,
            server_processing_seconds,
        ) = compute_ntp_timing_metrics(&tx);

        transactions.push(NtpTransactionReport {
            complete: tx.request.is_some() && tx.response.is_some(),
            notes: tx.notes.clone(),
            client_mac: tx.request.as_ref().and_then(|r| r.source_mac.clone() ),
            server_mac: tx.response.as_ref().and_then(|r| r.source_mac.clone() ),
            client_address: tx.request.as_ref().map(|r| r.source_address.to_string() ),
            server_address: tx.response.as_ref().map(|r| r.source_address.to_string() ),
            client_port: tx.request.as_ref().map(|r| r.source_port ),
            server_port: tx.response.as_ref().map(|r| r.source_port ),
            request_size: tx.request.as_ref().map(|r| r.size ),
            response_size: tx.response.as_ref().map(|r| r.size ),
            timestamp_client_transmit: tx.request.as_ref()
                .and_then(|r| r.transmit_timestamp.and_then(|t| t.to_datetime_utc())),
            timestamp_server_receive: tx.response.as_ref()
                .and_then(|r| r.receive_timestamp.and_then(|t| t.to_datetime_utc())),
            timestamp_server_transmit: tx.response.as_ref()
                .and_then(|r| r.transmit_timestamp.and_then(|t| t.to_datetime_utc())),
            timestamp_client_tap_receive: tx.request.as_ref().map(|r| r.timestamp ),
            timestamp_server_tap_receive: tx.response.as_ref().map(|r| r.timestamp ),
            server_version: tx.response.as_ref().map(|r| r.version ),
            client_version: tx.request.as_ref().map(|r| r.version ),
            server_mode: tx.response.as_ref().map(|r| r.mode ),
            client_mode: tx.request.as_ref().map(|r| r.mode ),
            stratum: tx.response.as_ref().map(|r| r.stratum ),
            leap_indicator: tx.response.as_ref().map(|r| r.leap_indicator ),
            precision: tx.response.as_ref().map(|r| r.precision ),
            poll_interval: tx.response.as_ref().map(|r| r.poll ),
            root_delay_seconds: tx.response.as_ref().map(|r| r.root_delay_seconds ),
            root_dispersion_seconds: tx.response.as_ref().map(|r| r.root_dispersion_seconds ),
            reference_id: tx.response.as_ref().map(|r| r.reference_id ),
            delay_seconds,
            offset_seconds,
            sensor_rtt_seconds,
            server_processing_seconds
        })
    }

    NtpTransactionsReport { transactions }
}


fn diff_seconds(a: DateTime<Utc>, b: DateTime<Utc>) -> f64 {
    let d: Duration = a - b;
    d.num_nanoseconds().unwrap_or(0) as f64 / 1e9
}

// TODO awful signature, change to returning a more descriptive struct
pub fn compute_ntp_timing_metrics(tx: &NtpTransaction)
        -> (Option<f64>, Option<f64>, Option<f64>, Option<f64>) {
    let req = match tx.request.as_ref() {
        Some(r) => r,
        None => return (None, None, None, None),
    };

    let resp = match tx.response.as_ref() {
        Some(r) => r,
        None => return (None, None, None, None),
    };

    // Sensor receive times.
    let s_req = req.timestamp;
    let s_resp = resp.timestamp;

    let sensor_rtt_seconds = {
        let v = diff_seconds(s_resp, s_req);
        if v.is_finite() && v >= 0.0 { Some(v) } else { None }
    };

    let server_processing_seconds = {
        let t2 = resp.receive_timestamp.as_ref().and_then(|t| t.to_datetime_utc());
        let t3 = resp.transmit_timestamp.as_ref().and_then(|t| t.to_datetime_utc());

        match (t2, t3) {
            (Some(t2), Some(t3)) => {
                let v = diff_seconds(t3, t2);
                if v.is_finite() && v >= 0.0 { Some(v) } else { None }
            }
            _ => None,
        }
    };

    let t1 = req.transmit_timestamp.as_ref().and_then(|t| t.to_datetime_utc());
    let t2 = resp.receive_timestamp.as_ref().and_then(|t| t.to_datetime_utc());
    let t3 = resp.transmit_timestamp.as_ref().and_then(|t| t.to_datetime_utc());

    let (delay_seconds, offset_seconds) = match (t1, t2, t3) {
        (Some(t1), Some(t2), Some(t3)) => {
            let t4_approx = s_resp;

            let delta = diff_seconds(t4_approx, t1) - diff_seconds(t3, t2);
            let delay = if delta.is_finite() && delta > -0.5 { Some(delta) } else { None };

            let theta = (diff_seconds(t2, t1) + diff_seconds(t3, t4_approx)) / 2.0;
            let offset = if theta.is_finite() { Some(theta) } else { None };

            (delay, offset)
        }
        _ => (None, None),
    };

    (delay_seconds, offset_seconds, sensor_rtt_seconds, server_processing_seconds)
}