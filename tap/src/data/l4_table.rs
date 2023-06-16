
use std::{sync::{Mutex, Arc}, collections::HashMap};

use chrono::{DateTime, Utc};
use log::error;
use strum_macros::Display;

use crate::{
    ethernet::packets::{UDPPacket, TCPPacket},
    link::payloads::{L4RetroPairReport, L4TableReport}
};

pub struct L4Table {
    retro_connections: Mutex<HashMap<L4Pair, L4PairMetrics>>
}

#[derive(Debug, Eq, PartialEq, Hash, Display)]
pub enum L4Type {
    Tcp, Udp
}

#[derive(Debug, Eq, PartialEq, Hash)]
pub struct L4Pair {
    pub l4_type: L4Type,
    pub source_mac: String,
    pub destination_mac: String,
    pub source_address: String,
    pub destination_address: String,
    pub source_port: u16,
    pub destination_port: u16,
    pub timestamp: DateTime<Utc>
}

#[derive(Debug)]
pub struct L4PairMetrics {
    pub connection_count: u64,
    pub size: u64
}

impl L4Table { 

    pub fn new() -> Self {
        Self {
            retro_connections: Mutex::new(HashMap::new())
        }
    }

    pub fn register_udp_pair(&mut self, udp: &Arc<UDPPacket>) {
        let pair = L4Pair {
            l4_type: L4Type::Udp,
            source_mac: udp.source_mac.clone(),
            destination_mac: udp.destination_mac.clone(),
            source_address: udp.source_address.clone(),
            destination_address: udp.destination_address.clone(),
            source_port: udp.source_port,
            destination_port: udp.destination_port,
            timestamp: udp.timestamp
        };

        Self::write_pair(&self.retro_connections, pair, u64::from(udp.size));
    }

    pub fn register_tcp_pair(&mut self, tcp: &Arc<TCPPacket>) {
        let pair = L4Pair {
            l4_type: L4Type::Tcp,
            source_mac: tcp.source_mac.clone(),
            destination_mac: tcp.destination_mac.clone(),
            source_address: tcp.source_address.clone(),
            destination_address: tcp.destination_address.clone(),
            source_port: tcp.source_port,
            destination_port: tcp.destination_port,
            timestamp: tcp.timestamp
        };

        Self::write_pair(&self.retro_connections, pair, u64::from(tcp.size));
    }

    fn write_pair(retro_connections: &Mutex<HashMap<L4Pair, L4PairMetrics>>, pair: L4Pair, size: u64) {
        match retro_connections.lock() {
            Ok(mut connections) => {
                connections.entry(pair)
                    .and_modify(|p| {
                        p.connection_count += 1;
                        p.size += size;
                    })
                    .or_insert(L4PairMetrics { connection_count: 1, size });
            },
            Err(e) => {
                error!("Could not acquire L4 pair connections table mutex: {}", e);
            }
        }
    }

    pub fn clear_ephemeral(&mut self) {
        match self.retro_connections.lock() {
            Ok(mut connections) => {
                connections.clear();
            },
            Err(e) => {
                error!("Could not acquire mutex to clear L4 pair connections table: {}", e);
            }
        }
    }

    pub fn to_report(&mut self) -> L4TableReport {
        let retro_pairs = match self.retro_connections.lock() {
            Ok(connections) => {
                let mut result = Vec::new();

                for (pair,metrics) in &*connections {
                    result.push(
                        L4RetroPairReport {
                            l4_type: pair.l4_type.to_string(),
                            source_mac: pair.source_mac.clone(),
                            destination_mac: pair.destination_mac.clone(),
                            source_address: pair.source_address.clone(),
                            destination_address: pair.destination_address.clone(),
                            source_port: pair.source_port,
                            destination_port: pair.destination_port,
                            connection_count: metrics.connection_count,
                            size: metrics.size,
                            timestamp: pair.timestamp
                        }
                    );
                }

                result
            },
            Err(e) => {
                error!("Could not acquire L4 pair connections mutex. {}", e);

                Vec::new()
            }
        };

        L4TableReport { 
            retro_pairs
         }
    }

}