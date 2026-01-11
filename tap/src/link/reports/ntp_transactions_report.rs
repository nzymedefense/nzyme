use std::collections::{HashMap, HashSet};
use std::fmt::Display;
use std::sync::MutexGuard;
use chrono::{DateTime, Duration, Utc};
use log::warn;
use serde::{Serialize, Serializer};
use crate::protocols::parsers::ntp_parser::NtpReferenceId;
use crate::state::tables::ntp_table::NtpTransaction;

#[derive(Serialize)]
pub struct NtpTransactionsReport {
    transactions: Vec<NtpTransactionReport>
}

fn serialize_option_display<S, T>(value: &Option<T>, serializer: S, ) -> Result<S::Ok, S::Error>
where
    S: Serializer,
    T: Display
{
    match value {
        Some(v) => serializer.serialize_str(&v.to_string()),
        None => serializer.serialize_none(),
    }
}

#[derive(Serialize)]
pub struct NtpTransactionReport {
    pub complete: bool,
    pub notes: HashSet<String>,
    pub client_mac: Option<String>,
    pub server_mac: Option<String>,
    pub client_address: String,
    pub server_address: String,
    pub client_port: u16,
    pub server_port: u16,
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
    #[serde(serialize_with = "serialize_option_display")]
    pub reference_id: Option<NtpReferenceId>,
    pub delay_seconds: Option<f64>,
    pub offset_seconds: Option<f64>,
    pub rtt_seconds: Option<f64>,
    pub server_processing_seconds: Option<f64>
}

pub fn generate(txs: &Vec<NtpTransaction>) -> NtpTransactionsReport {
    let mut transactions: Vec<NtpTransactionReport> = Vec::new();

    for tx in txs {
        let (
            delay_seconds,
            offset_seconds,
            rtt_seconds,
            server_processing_seconds,
        ) = compute_ntp_timing_metrics(&tx);

        /*
         * We may not always have request and response, but we need MAC and address of both sides
         * to build transactions and for analysis. This is why, if one side is missing, we simply
         * take the data from the other side. For example, if we don't have a request, we take the
         * destination of the response.
         */
        let (client_mac, client_address, client_port) = match tx.request.clone() {
            Some(request) => {
                // We have a request and can use it directly.
                (request.source_mac, request.source_address.to_string(), request.source_port)
            },
            None => {
                // We don't have a request. Use the response for client fields.
                match tx.response.clone() {
                    Some(response) => {
                        (response.destination_mac,
                         response.destination_address.to_string(),
                         response.destination_port)
                    },
                    None => {
                        warn!("Recorded NTP transaction without request or response. Skipping.");
                        continue;
                    }
                }
            }
        };
        let (server_mac, server_address, server_port) = match tx.response.clone() {
            Some(response) => {
                // We have a response and can use it directly.
                (response.source_mac, response.source_address.to_string(), response.source_port)
            },
            None => {
                // We don't have a response. Use the request for server fields.
                match tx.request.clone() {
                    Some(request) => {
                        (request.destination_mac,
                         request.destination_address.to_string(),
                         request.destination_port)
                    },
                    None => {
                        warn!("Recorded NTP transaction without request or response. Skipping.");
                        continue;
                    }
                }
            }
        };

        transactions.push(NtpTransactionReport {
            complete: tx.request.is_some() && tx.response.is_some(),
            notes: tx.notes.iter().map(|note| note.to_string()).collect(),
            client_mac,
            server_mac,
            client_address,
            server_address,
            client_port,
            server_port,
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
            reference_id: tx.response.as_ref().map(|r| r.reference_id.clone() ),
            delay_seconds,
            offset_seconds,
            rtt_seconds,
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

    let s_req = req.timestamp;
    let s_resp = resp.timestamp;

    let rtt_seconds = {
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

    let t1_is_realistic = t1.map(|t1| diff_seconds(s_req, t1).abs() <= 10.0).unwrap_or(false);

    let (delay_seconds, offset_seconds) = match (t1, t2, t3) {
        (Some(t1), Some(t2), Some(t3)) if t1_is_realistic => {
            let t4_approx = s_resp;

            let delta = diff_seconds(t4_approx, t1) - diff_seconds(t3, t2);
            let delay = if delta.is_finite() {
                if delta >= 0.0 { Some(delta) }
                else if delta >= -0.5 { Some(0.0) } // optional clamp
                else { None }
            } else {
                None
            };

            let theta = (diff_seconds(t2, t1) + diff_seconds(t3, t4_approx)) / 2.0;
            let offset = if theta.is_finite() { Some(theta) } else { None };

            (delay, offset)
        }
        _ => (None, None),
    };

    (delay_seconds, offset_seconds, rtt_seconds, server_processing_seconds)
}