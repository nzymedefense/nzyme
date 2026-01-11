use std::collections::{HashMap, HashSet};
use std::hash::{Hash, Hasher};
use std::sync::{Arc, Mutex};
use anyhow::{bail, Error};
use chrono::{Duration, Utc};
use log::{error};
use strum_macros::Display;
use crate::helpers::timer::{record_timer, Timer};
use crate::link::leaderlink::Leaderlink;
use crate::link::reports::ntp_transactions_report;
use crate::metrics::Metrics;
use crate::protocols::parsers::l4_key::L4Key;
use crate::state::tables::ntp_table::NtpTransactionNote::{ClientT1Missing, ClientT1Zero, DuplicateRequest, DuplicateResponse, ServerOriginMissing, ServerOriginZero};
use crate::wired::packets::{NtpPacket, NtpPacketType, NtpTimestamp};

pub struct NtpTable {
    metrics: Arc<Mutex<Metrics>>,
    leaderlink: Arc<Mutex<Leaderlink>>,
    transactions: Mutex<HashMap<L4Key, NtpTransaction>>,
}

#[derive(Debug)]
pub struct NtpTransaction {
    pub request: Option<NtpPacket>,
    pub response: Option<NtpPacket>,
    pub notes: HashSet<NtpTransactionNote>
}

#[derive(Debug, Display, Hash, Eq, PartialEq)]
pub enum NtpTransactionNote {
    ClientT1Zero,
    ClientT1Missing,
    ServerOriginZero,
    ServerOriginMissing,
    DuplicateRequest,
    DuplicateResponse
}

impl NtpTable {
    pub fn new(leaderlink: Arc<Mutex<Leaderlink>>, metrics: Arc<Mutex<Metrics>>) -> Self {
        Self {
            metrics,
            leaderlink,
            transactions: Mutex::new(HashMap::new()),
        }
    }

    pub fn register_ntp_packet(&self, packet: Arc<NtpPacket>) {
        let key = L4Key::new(
            packet.source_address,
            packet.source_port,
            packet.destination_address,
            packet.destination_port,
        );

        match self.transactions.lock() {
            Ok(mut transactions) => {
                let tx = transactions.entry(key).or_insert_with(|| NtpTransaction {
                    request: None,
                    response: None,
                    notes: HashSet::new(),
                });

                // Add notes.
                match packet.ntp_type {
                    NtpPacketType::Client => {
                        if let Some(t1) = packet.transmit_timestamp.as_ref() {
                            if t1.seconds == 0 && t1.fraction == 0 {
                                tx.notes.insert(ClientT1Zero);
                            }
                        } else {
                            tx.notes.insert(ClientT1Missing);
                        }

                        if tx.request.is_none() {
                            tx.request = Some(packet.as_ref().clone());
                        } else {
                            tx.notes.insert(DuplicateRequest);
                        }
                    }
                    NtpPacketType::Server => {
                        if let Some(o) = packet.origin_timestamp.as_ref() {
                            if o.seconds == 0 && o.fraction == 0 {
                                tx.notes.insert(ServerOriginZero);
                            }
                        } else {
                            tx.notes.insert(ServerOriginMissing);
                        }

                        if tx.response.is_none() {
                            tx.response = Some(packet.as_ref().clone());
                        } else {
                            tx.notes.insert(DuplicateResponse);
                        }
                    }
                }
            }
            Err(e) => {
                error!("Could not acquire NTP transactions mutex: {}", e);
            }
        }
    }

    pub fn process_report(&self) {
        /*
         * We report all transactions that are at least 5 seconds old. This gives them
         * plenty of time to complete, while we can still detect things like multiple
         * responses to a single request etc.
         *
         * This step also removes those transactions from the table.
         */
        match self.filter_and_clear_transactions_older_than(Duration::seconds(5)) {
            Ok(filtered) => {
                // Generate JSON.
                let mut timer = Timer::new();
                let report = match serde_json::to_string(
                        &ntp_transactions_report::generate(&filtered)) {
                    Ok(report) => report,
                    Err(e) => {
                        error!("Could not serialize NTP transactions report: {}", e);
                        return;
                    }
                };
                timer.stop();
                record_timer(
                    timer.elapsed_microseconds(),
                    "tables.ntp.timer.report_generation",
                    &self.metrics
                );

                // Send report.
                match self.leaderlink.lock() {
                    Ok(link) => {
                        if let Err(e) = link.send_report("ntp/transactions", report) {
                            error!("Could not submit NTP transactions report: {}", e);
                        }
                    },
                    Err(e) => error!("Could not acquire leader link lock for NTP \
                        transactions report submission: {}", e)
                }
            },
            Err(e) => {
                error!("Could not filter NTP transactions for report processing: {}", e)
            }
        }
    }

    pub fn filter_and_clear_transactions_older_than(&self, min_age: Duration)
            -> Result<Vec<NtpTransaction>, Error> {
        let cutoff = Utc::now() - min_age;

        match self.transactions.lock() {
            Ok(mut transactions) => {
                // Collect the keys to remove.
                let keys_to_take: Vec<L4Key> = transactions
                    .iter()
                    .filter_map(|(k, tx)| {
                        let last_seen = match (tx.request.as_ref(), tx.response.as_ref()) {
                            (Some(req), Some(resp)) => {
                                if resp.timestamp > req.timestamp { resp.timestamp } else { req.timestamp }
                            }
                            (Some(req), None) => req.timestamp,
                            (None, Some(resp)) => resp.timestamp,
                            (None, None) => return None,
                        };

                        if last_seen <= cutoff { Some(k.clone()) } else { None }
                    }).collect();

                // Remove and return them
                Ok(keys_to_take
                    .into_iter()
                    .filter_map(|k| transactions.remove(&k))
                    .collect())
            },
            Err(e) => {
                bail!("Could not acquire NTP transactions mutex: {}", e);
            }
        }
    }

    pub fn calculate_metrics(&self) {
        let transaction_size: i128 = match self.transactions.lock() {
            Ok(c) => c.len() as i128,
            Err(e) => {
                error!("Could not acquire mutex to calculate NTP transactions table size: {}", e);
                -1
            }
        };

        match self.metrics.lock() {
            Ok(mut metrics) => {
                metrics.set_gauge("tables.ntp.transactions.size", transaction_size);
            },
            Err(e) => error!("Could not acquire metrics mutex: {}", e)
        }
    }

}