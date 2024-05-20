use std::{u128, collections::HashMap, sync::{Mutex, Arc}};
use std::net::IpAddr;

use crate::{
    ethernet::{
        packets::DNSPacket,
        types::DNSType
    },
    link::payloads::{
        DnsTableReport,
        DnsIpStatisticsReport,
        DNSEntropyLog
    },
    metrics::Metrics
};
use chrono::{DateTime, Utc};
use log::{error};
use crate::data::table_helpers;
use crate::data::table_helpers::{clear_mutex_hashmap, clear_mutex_vector};
use crate::link::leaderlink::Leaderlink;
use crate::link::payloads::{DNSLogReport};

pub struct DnsTable {
    leaderlink: Arc<Mutex<Leaderlink>>,
    ips: Mutex<HashMap<IpAddr, DnsStatistics>>,
    entropy_log: Mutex<Vec<EntropyLog>>,
    pairs: Mutex<HashMap<IpAddr, Mutex<HashMap<IpAddr, u128>>>>,
    metrics: Arc<Mutex<Metrics>>,
    query_log: Mutex<Vec<DNSQueryLog>>,
    response_log: Mutex<Vec<DNSResponseLog>>
}

#[derive(Default, Debug)]
pub struct DnsStatistics {
    request_count: u128,
    request_bytes: u128,
    response_count: u128,
    response_bytes: u128,
    nxdomain_count: u128
}

#[derive(Debug)]
pub struct EntropyLog {
    transaction_id: u16,
    entropy: f32,
    entropy_mean: f32,
    zscore: f32,
    timestamp: DateTime<Utc>
}

#[derive(Debug)]
pub struct DNSQueryLog {
    transaction_id: Option<u16>,
    client_address: IpAddr,
    server_address: IpAddr,
    client_mac: String,
    server_mac: String,
    client_port: u16,
    server_port: u16,
    query_value: String,
    query_value_etld: Option<String>,
    data_type: String,
    timestamp: DateTime<Utc>
}

#[derive(Debug)]
pub struct DNSResponseLog {
    transaction_id: Option<u16>,
    client_address: IpAddr,
    server_address: IpAddr,
    client_mac: String,
    server_mac: String,
    client_port: u16,
    server_port: u16,
    response_value: String,
    response_value_etld: Option<String>,
    data_type: String,
    timestamp: DateTime<Utc>
}

impl DnsTable {

    pub fn new(metrics: Arc<Mutex<Metrics>>, leaderlink: Arc<Mutex<Leaderlink>>) -> Self {
        DnsTable {
            leaderlink,
            ips: Mutex::new(HashMap::new()),
            entropy_log: Mutex::new(Vec::new()),
            pairs: Mutex::new(HashMap::new()),
            query_log: Mutex::new(Vec::new()),
            response_log: Mutex::new(Vec::new()),
            metrics
        }
    }

    pub fn register_request(&mut self, request: &Arc<DNSPacket>) {
        if !matches!(request.dns_type, DNSType::Query) {
            error!("Attempted to register unexpected type [{:?}] as DNS request.", request.dns_type);
            return;
        }

        let source_address = &request.source_address;
        let destination_address = &request.destination_address;
        match self.ips.lock() {
            Ok(mut ips) => {
                match &mut ips.get(source_address) {
                    Some(ip) => {
                        let request_count = ip.request_count+1;
                        let request_bytes = ip.request_bytes+(u128::from(request.size));
                        let response_count = ip.response_count;
                        let response_bytes = ip.response_bytes;
                        let nxdomain_count = ip.nxdomain_count;

                        // Replace entire statistics field.
                        ips.insert(
                            *source_address,
                            DnsStatistics {
                                request_count,
                                request_bytes,
                                response_count,
                                response_bytes,
                                nxdomain_count
                            }
                        );
                    },
                    None => {
                        ips.insert(
                            *source_address,
                            DnsStatistics {
                                request_count: 1,
                                request_bytes: u128::from(request.size),
                                response_count: 0,
                                response_bytes: 0,
                                nxdomain_count: 0
                            }
                        );
                    }
                }
            },
            Err(e) => {
                error!("Could not acquire DNS IP table mutex: {}", e);
            }
        }

        match self.pairs.lock() {
            Ok(mut pairs) => {
                if !pairs.contains_key(source_address) {
                    pairs.insert(*source_address, Mutex::new(HashMap::new()));
                }

                let counter = pairs.get(source_address).unwrap();

                match counter.lock() {
                    Ok(mut counter) => {
                        if counter.contains_key(destination_address) {
                            let new = counter.get(destination_address).unwrap()+1;
                            counter.insert(*destination_address, new);
                        } else {
                            counter.insert(*destination_address, 1);
                        }
                    },
                    Err(e) => {
                        error!("Could not acquire DNS pair counter table mutex: {}", e);
                    }
                }
            },
            Err(e) => {
                error!("Could not acquire DNS pairs table mutex: {}", e);
            }
        };

        // Raw queries.
        match self.query_log.lock() {
            Ok(mut retro) => {
                if let Some(queries) = &request.queries {
                    for query_data in queries {
                        retro.push(DNSQueryLog {
                            transaction_id: request.transaction_id,
                            client_address: request.source_address,
                            server_address: request.destination_address,
                            client_mac: request.source_mac.clone(),
                            server_mac: request.destination_mac.clone(),
                            client_port: request.source_port,
                            server_port: request.destination_port,
                            query_value: query_data.name.clone(),
                            query_value_etld: query_data.name_etld.clone(),
                            data_type: query_data.dns_type.to_string(),
                            timestamp: request.timestamp
                        });
                    }
                }
            },
            Err(e) => error!("Could not acquire retro query log mutex: {}", e)
        }
    }
    
    pub fn register_response(&mut self, response: &Arc<DNSPacket>) {
        if !matches!(response.dns_type, DNSType::QueryResponse) {
            error!("Attempted to register unexpected type [{:?}] as DNS response.", response.dns_type);
            return;
        }

        let is_nxdomain = response.answer_count == 0;

        let destination_address = &response.destination_address;
        match self.ips.lock() {
            Ok(mut ips) => {
                match &mut ips.get(destination_address) {
                    Some(ip) => {
                        let request_count = ip.request_count;
                        let request_bytes = ip.request_bytes;
                        let response_count = ip.response_count+1;
                        let response_bytes = ip.response_bytes+(u128::from(response.size));

                    
                        let nxdomain_count = if is_nxdomain {
                            ip.nxdomain_count+1
                        } else {
                            ip.nxdomain_count
                        };

                        // Replace entire statistics field.
                        ips.insert(
                            *destination_address,
                            DnsStatistics {
                                request_count,
                                request_bytes,
                                response_count,
                                response_bytes,
                                nxdomain_count
                            }
                        );                        
                    },
                    None => {
                        ips.insert(
                            *destination_address,
                            DnsStatistics {
                                request_count: 0,
                                request_bytes: 0,
                                response_count: 1,
                                response_bytes: u128::from(response.size),
                                nxdomain_count: 0
                            }
                        );
                    }
                }
            },
            Err(e) => {
                error!("Could not acquire DNS IP table mutex: {}", e);
            }
        }

        // Log.
        match self.response_log.lock() {
            Ok(mut log) => {
                if let Some(responses) = &response.responses {
                    for response_data in responses {
                        if let Some(response_value) = &response_data.value {
                            log.push(DNSResponseLog {
                                transaction_id: response.transaction_id,
                                client_address: response.destination_address,
                                server_address: response.source_address,
                                client_mac: response.destination_mac.clone(),
                                server_mac: response.source_mac.clone(),
                                client_port: response.destination_port,
                                server_port: response.source_port,
                                response_value: response_value.to_string(),
                                response_value_etld: response_data.value_etld.clone(),
                                data_type: response_data.dns_type.to_string(),
                                timestamp: response.timestamp
                            });
                        }
                    }
                }
            },
            Err(e) => error!("Could not acquire retro response log mutex: {}", e)
        }

    }

    pub fn register_exceeded_entropy(&mut self,
                                     transaction_id: u16,
                                     entropy: f32,
                                     zscore: f32,
                                     entropy_mean: f32,
                                     timestamp: DateTime<Utc>) {
        match self.entropy_log.lock() {
            Ok(mut log) => log.push(
                EntropyLog { transaction_id, entropy, zscore, entropy_mean, timestamp }
            ),
            Err(e) => error!("Could not acquire entropy log mutex: {}", e)
        }
    }

    pub fn process_report(&self) {
        // Generate JSON.
        let report = match serde_json::to_string(&self.generate_report()) {
            Ok(report) => report,
            Err(e) => {
                error!("Could not serialize DNS report: {}", e);
                return;
            }
        };

        // Send report.
        match self.leaderlink.lock() {
            Ok(link) => {
                if let Err(e) = link.send_report("dns/summary", report) {
                    error!("Could not submit DNS report: {}", e);
                }
            },
            Err(e) => error!("Could not acquire DNS table lock for report submission: {}", e)
        }

        // Clean up.
        clear_mutex_hashmap(&self.ips);
        clear_mutex_hashmap(&self.pairs);
        clear_mutex_vector(&self.query_log);
        clear_mutex_vector(&self.response_log);
        clear_mutex_vector(&self.entropy_log);
    }

    #[allow(clippy::too_many_lines)]
    fn generate_report(&self) -> DnsTableReport {
        let mut ips = HashMap::new();
        match self.ips.lock() {
            Ok(x) => {
                for (ip, data) in &*x {
                    ips.insert(ip.to_string(), DnsIpStatisticsReport{
                        request_count: data.request_count,
                        request_bytes: data.request_bytes,
                        response_count: data.response_count,
                        response_bytes: data.response_bytes,
                        nxdomain_count: data.nxdomain_count
                    });
                }
            },
            Err(e) => error!("Could not acquire DNS IPs table mutex: {}", e)
        }

        let mut entropy_log = Vec::new();
        match self.entropy_log.lock() {
            Ok(x) => {
                for log in &*x {
                    entropy_log.push(DNSEntropyLog {
                        transaction_id: log.transaction_id,
                        entropy: log.entropy,
                        zscore: log.zscore,
                        entropy_mean: log.entropy_mean,
                        timestamp: log.timestamp
                    });
                }
            },
            Err(e) => error!("Could not acquire DNS entropy table mutex: {}", e)
        }

        let pairs = match self.pairs.lock() {
            Ok(pairs) => {
                let mut result = HashMap::new();

                for (source, counter) in &*pairs {
                    let mut counter_result = HashMap::new();

                    match counter.lock() {
                        Ok(counter) => {
                            for (destination, count) in &*counter {
                                counter_result.insert(destination.to_string(), *count);
                            } 
                        },
                        Err(e) => error!("Could not acquire DNS pairs counter table mutex: {}", e)
                    }

                    result.insert(source.to_string(), counter_result);
                }

                result
            }
            Err(e) => {
                error!("Could not acquire DNS pairs table mutex: {}", e);
                HashMap::new()
            }
        };

        let queries = match self.query_log.lock() {
            Ok(retro) => {
                let mut result = Vec::new();

                for r in &*retro {
                    result.push(DNSLogReport {
                        transaction_id: r.transaction_id,
                        client_address: r.client_address.to_string(),
                        server_address: r.server_address.to_string(),
                        client_mac: r.client_mac.clone(),
                        server_mac: r.server_mac.clone(),
                        client_port: r.client_port,
                        server_port: r.server_port,
                        data_value: r.query_value.clone(),
                        data_value_etld: r.query_value_etld.clone(),
                        data_type: r.data_type.clone(),
                        timestamp: r.timestamp,
                    });
                }

                result
            },
            Err(e) => {
                error!("Could not acquire DNS retro queries mutex: {}", e);
                Vec::new()
            }
        };
        
        let responses = match self.response_log.lock() {
            Ok(retro) => {
                let mut result = Vec::new();

                for r in &*retro {
                    result.push(DNSLogReport {
                        transaction_id: r.transaction_id,
                        client_address: r.client_address.to_string(),
                        server_address: r.server_address.to_string(),
                        client_mac: r.client_mac.clone(),
                        server_mac: r.server_mac.clone(),
                        client_port: r.client_port,
                        server_port: r.server_port,
                        data_value: r.response_value.clone(),
                        data_value_etld: r.response_value_etld.clone(),
                        data_type: r.data_type.clone(),
                        timestamp: r.timestamp,
                    });
                }

                result
            },
            Err(e) => {
                error!("Could not acquire DNS retro responses mutex: {}", e);
                Vec::new()
            }
        };

        DnsTableReport {
            ips,
            entropy_log,
            pairs,
            queries,
            responses
        }
    }

    pub fn calculate_metrics(&self) {
        match self.metrics.lock() {
            Ok(mut metrics) => {
                metrics.set_gauge("tables.dns.ips.size",
                                  table_helpers::get_mutex_hashmap_size(&self.ips));
                metrics.set_gauge("tables.dns.entropy_log.size",
                                  table_helpers::get_mutex_vector_size(&self.entropy_log));
                metrics.set_gauge("tables.dns.pairs.size",
                                  table_helpers::get_mutex_hashmap_size(&self.pairs));
                metrics.set_gauge("tables.dns.query_log.size",
                                  table_helpers::get_mutex_vector_size(&self.query_log));
                metrics.set_gauge("tables.dns.response_log.size",
                                  table_helpers::get_mutex_vector_size(&self.response_log));
            },
            Err(e) => error!("Could not acquire metrics mutex: {}", e)
        }
    }

}
