use std::collections::{HashMap, HashSet};
use std::hash::{Hash, Hasher};
use std::sync::{Arc, Mutex};
use anyhow::{bail, Error};
use chrono::{Duration, Utc};
use log::error;
use crate::helpers::timer::{record_timer, Timer};
use crate::link::leaderlink::Leaderlink;
use crate::link::reports::ntp_transactions_report;
use crate::metrics::Metrics;
use crate::protocols::parsers::l4_key::L4Key;
use crate::wired::packets::{NtpPacket, NtpPacketType, NtpTimestamp};

pub struct NtpTable {
    metrics: Arc<Mutex<Metrics>>,
    leaderlink: Arc<Mutex<Leaderlink>>,
    transactions: Mutex<HashMap<NtpTransactionKey, NtpTransaction>>,
}

#[derive(Debug)]
pub struct NtpTransaction {
    pub request: Option<NtpPacket>,
    pub response: Option<NtpPacket>,
    pub notes: HashSet<String>
}

#[derive(Copy, Clone, Debug, Eq, PartialEq, Hash)]
pub struct NtpTsKey(pub u64);

impl NtpTsKey {
    pub fn from_ntp(ts: &NtpTimestamp) -> Self {
        NtpTsKey(((ts.seconds as u64) << 32) | (ts.fraction as u64))
    }
}

/*
 * Our transaction key is built from the standard Layer 4 key and a NTP "Timestamp" key. We
 * use it to correlate request/response based on the `transmit_timestamp` and `origin_timestamp`
 * matching in both packets. Some NTP clients set their source port to the standard NTP port of
 * `123`, which would break our L4-only correlation.
 */
#[derive(Clone, Debug, Eq)]
pub struct NtpTransactionKey {
    pub l4: L4Key,
    pub t1: NtpTsKey,
}

impl PartialEq for NtpTransactionKey {
    fn eq(&self, other: &Self) -> bool {
        self.l4 == other.l4 && self.t1 == other.t1
    }
}

impl Hash for NtpTransactionKey {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.l4.hash(state);
        self.t1.hash(state);
    }
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
        let l4 = L4Key::new(
            packet.source_address,
            packet.source_port,
            packet.destination_address,
            packet.destination_port,
        );

        let key = match packet.ntp_type {
            NtpPacketType::Client => {
                let t1 = match packet.transmit_timestamp.as_ref() {
                    Some(t1) => t1,
                    None => {
                        // TODO add note: client packet missing transmit timestamp
                        return;
                    }
                };

                NtpTransactionKey { l4, t1: NtpTsKey::from_ntp(t1), }
            }

            NtpPacketType::Server => {
                let t1_echo = match packet.origin_timestamp.as_ref() {
                    Some(o) => o,
                    None => {
                        // TODO add note: server packet missing origin timestamp
                        return;
                    }
                };

                NtpTransactionKey { l4, t1: NtpTsKey::from_ntp(t1_echo), }
            }
        };

        match self.transactions.lock() {
            Ok(mut transactions) => {
                let tx = transactions.entry(key).or_insert_with(|| NtpTransaction {
                    request: None,
                    response: None,
                    notes: HashSet::new()
                });

                match packet.ntp_type {
                    NtpPacketType::Client => {
                        if tx.request.is_none() {
                            tx.request = Some(packet.as_ref().clone());
                        }
                    }
                    NtpPacketType::Server => {
                        if tx.response.is_none() {
                            tx.response = Some(packet.as_ref().clone());
                        }
                    }
                }
            },
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
                let keys_to_take: Vec<NtpTransactionKey> = transactions
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
                    })
                    .collect();

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
