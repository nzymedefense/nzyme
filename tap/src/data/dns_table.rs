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
        NXDomainLogReport,
        DNSEntropyLog,
        DNSRetroQueryLogReport,
        DNSRetroResponseLogReport
    },
    metrics::Metrics
};
use chrono::{DateTime, Utc};
use log::error;
use crate::data::table_helpers::{clear_mutex_hashmap, clear_mutex_vector};
use crate::link::leaderlink::Leaderlink;

pub struct DnsTable {
    leaderlink: Arc<Mutex<Leaderlink>>,
    ips: Mutex<HashMap<IpAddr, DnsStatistics>>,
    nxdomains: Mutex<Vec<NXDomainLog>>,
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
pub struct NXDomainLog {
    ip: IpAddr,
    server: IpAddr,
    query_value: String,
    data_type: String,
    timestamp: DateTime<Utc>
}

#[derive(Debug)]
pub struct EntropyLog {
    log_type: String,
    entropy: f32,
    zscore: f32,
    value: String,
    timestamp: DateTime<Utc>
}

pub struct DNSQueryLog {
    ip: IpAddr,
    server: IpAddr,
    source_mac: String,
    destination_mac: String,
    port: u16,
    query_value: String,
    data_type: String,
    timestamp: DateTime<Utc>
}

pub struct DNSResponseLog {
    ip: IpAddr,
    server: IpAddr,
    source_mac: String,
    destination_mac: String,
    response_value: String,
    data_type: String,
    timestamp: DateTime<Utc>
}

impl DnsTable {

    pub fn new(metrics: Arc<Mutex<Metrics>>, leaderlink: Arc<Mutex<Leaderlink>>) -> Self {
        DnsTable {
            leaderlink,
            ips: Mutex::new(HashMap::new()),
            nxdomains: Mutex::new(Vec::new()),
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
                            source_address.clone(),
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
                            source_address.clone(),
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
                    pairs.insert(source_address.clone(), Mutex::new(HashMap::new()));
                }

                let counter = pairs.get(source_address).unwrap();

                match counter.lock() {
                    Ok(mut counter) => {
                        if counter.contains_key(destination_address) {
                            let new = counter.get(destination_address).unwrap()+1;
                            counter.insert(destination_address.clone(), new);
                        } else {
                            counter.insert(destination_address.clone(), 1);
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
                            ip: request.source_address.clone(),
                            server: request.destination_address.clone(),
                            source_mac: request.source_mac.clone(),
                            destination_mac: request.destination_mac.clone(),
                            port: request.destination_port,
                            query_value: query_data.name.clone(),
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
                            destination_address.clone(),
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
                            destination_address.clone(),
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

        // Add to NXDOMAIN log if it is one.
        if is_nxdomain {
            match self.nxdomains.lock() {
                Ok(mut nxdomains) => {
                    // TODO: How to handle multiple queries/responses? That never really happens.
                    if let Some(queries) = &response.queries {
                        for query in queries {
                            nxdomains.push(NXDomainLog {
                                    ip: response.destination_address.clone(),
                                    server: response.source_address.clone(),
                                    query_value: query.name.clone(),
                                    data_type: query.dns_type.to_string(),
                                    timestamp: response.timestamp
                            });
                        }
                    }
                },
                Err(e) => error!("Could not acquire NXDOMAIN log mutex: {}", e)
            }
        }

        // Retro.
        match self.response_log.lock() {
            Ok(mut retro) => {
                if let Some(responses) = &response.responses {
                    for response_data in responses {
                        if let Some(response_value) = &response_data.value {
                            retro.push(DNSResponseLog {
                                ip: response.destination_address.clone(),
                                server: response.source_address.clone(),
                                source_mac: response.source_mac.clone(),
                                destination_mac: response.destination_mac.clone(),
                                response_value: response_value.to_string(),
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

    pub fn register_exceeded_entropy(&mut self, timestamp: DateTime<Utc>, log_type: String, entropy: f32, zscore: f32, value: String) {
        match self.entropy_log.lock() {
            Ok(mut log) => log.push(EntropyLog { log_type, entropy, zscore, value, timestamp }),
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
        clear_mutex_vector(&self.nxdomains);
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

        let mut nxdomains = Vec::new();
        match self.nxdomains.lock() {
            Ok(x) => {
                for nxdomain in &*x {
                    nxdomains.push(NXDomainLogReport {
                        ip: nxdomain.ip.to_string(),
                        server: nxdomain.server.to_string(),
                        query_value: nxdomain.query_value.clone(),
                        data_type: nxdomain.data_type.clone(),
                        timestamp: nxdomain.timestamp
                    });
                }
            },
            Err(e) => error!("Could not acquire DNS NXDOMAINs table mutex: {}", e)
        }

        let mut entropy_log = Vec::new();
        match self.entropy_log.lock() {
            Ok(x) => {
                for log in &*x {
                    entropy_log.push(DNSEntropyLog {
                        log_type: log.log_type.clone(),
                        entropy: log.entropy,
                        zscore: log.zscore,
                        value: log.value.clone(),
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

        let retro_queries = match self.query_log.lock() {
            Ok(retro) => {
                let mut result = Vec::new();

                for r in &*retro {
                    result.push(DNSRetroQueryLogReport {
                        ip: r.ip.to_string(),
                        server: r.server.to_string(),
                        source_mac: r.source_mac.clone(),
                        destination_mac: r.destination_mac.clone(),
                        port: r.port,
                        query_value: r.query_value.clone(),
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

        
        let retro_responses = match self.response_log.lock() {
            Ok(retro) => {
                let mut result = Vec::new();

                for r in &*retro {
                    result.push(DNSRetroResponseLogReport {
                        ip: r.ip.to_string(),
                        server: r.server.to_string(),
                        source_mac: r.source_mac.clone(),
                        destination_mac: r.destination_mac.clone(),
                        response_value: r.response_value.clone(),
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
            nxdomains,
            entropy_log,
            pairs,
            retro_queries,
            retro_responses
        }
    }

    pub fn calculate_metrics(&self) {
        let ips_size: i128 = match self.ips.lock() {
            Ok(ips) => ips.len() as i128,
            Err(e) => {
                error!("Could not acquire mutex to calculate DNS IPs table size: {}", e);

                -1
            }
        };

        let nxdomains_size: i128 = match self.nxdomains.lock() {
            Ok(nxdomains) => nxdomains.len() as i128,
            Err(e) => {
                error!("Could not acquire mutex to calculate NXDOMAIN table size: {}", e);

                -1
            }
        };

        let entropylog_size: i128 = match self.entropy_log.lock() {
            Ok(log) => log.len() as i128,
            Err(e) => {
                error!("Could not acquire mutex to calculate DNS entropy table size: {}", e);

                -1
            }
        };

        match self.metrics.lock() {
            Ok(mut metrics) => {
                metrics.set_gauge("tables.dns.ips.size", ips_size);
                metrics.set_gauge("tables.dns.nxdomains.size", nxdomains_size);
                metrics.set_gauge("tables.dns.entropy_log.size", entropylog_size);
            },
            Err(e) => error!("Could not acquire metrics mutex: {}", e)
        }
    }

}
