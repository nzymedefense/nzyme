use std::collections::HashMap;
use std::hash::{Hash, Hasher};
use std::sync::{Arc, Mutex};
use log::error;
use crate::link::leaderlink::Leaderlink;
use crate::metrics::Metrics;
use crate::protocols::parsers::l4_key::L4Key;
use crate::state::tables::table_helpers::clear_mutex_hashmap;
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
                        // TODO trigger alert: client packet missing transmit timestamp
                        return;
                    }
                };

                NtpTransactionKey { l4, t1: NtpTsKey::from_ntp(t1), }
            }

            NtpPacketType::Server => {
                let t1_echo = match packet.origin_timestamp.as_ref() {
                    Some(o) => o,
                    None => {
                        // TODO trigger alert: server packet missing origin timestamp
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
        match self.transactions.lock() {
            Ok(mut txs) => {
                // TODO

                // Report complete transactions. Give them 5 seconds to accum some notes.
                // Report incomplete/failed transactions if no activity in 5 sec

            },
            Err(e) => {
                error!("Could not acquire NTP transactions mutex: {}", e);
            }
        }

        // TODO: REPLACE WITH REAL CLEANING
        clear_mutex_hashmap(&self.transactions);
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
