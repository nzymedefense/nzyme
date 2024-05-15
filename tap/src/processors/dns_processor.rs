use std::{sync::{Mutex, Arc}, collections::HashMap, thread};

use crate::{data::dns_table::DnsTable, ethernet::{packets::DNSPacket, types::DNSType}, helpers::math, system_state::SystemState, metrics::Metrics};

use chrono::{DateTime, Utc, Duration};
use clokwerk::{Scheduler, TimeUnits};
use log::{error, debug};

pub struct DnsProcessor {
    system_state: Arc<SystemState>,
    dns_table: Arc<Mutex<DnsTable>>,
    query_entropy: Arc<Mutex<HashMap<DateTime<Utc>, f32>>>,
    response_entropy: Arc<Mutex<HashMap<DateTime<Utc>, f32>>>,
    entropy_zscore_threshold: f32
}

struct ZScoreResult {
    zscore: f32,
    mean: f32
}

impl DnsProcessor {

    pub fn new(system_state: Arc<SystemState>, dns_table: Arc<Mutex<DnsTable>>, metrics: Arc<Mutex<Metrics>>) -> Self {
        let query_entropy = Arc::new(Mutex::new(HashMap::new()));
        let query_entropy_ret = query_entropy.clone();
        let query_entropy_metrics = query_entropy.clone();

        let response_entropy = Arc::new(Mutex::new(HashMap::new()));
        let response_entropy_ret = response_entropy.clone();
        let response_entropy_metrics = response_entropy.clone();

        // Clean and update entropy table.
        let mut scheduler = Scheduler::new();
        scheduler.every(5.minutes()).run(move || {
            Self::clean_entropy_table(&query_entropy_ret.clone());
            Self::clean_entropy_table(&response_entropy_ret.clone());
        });

        scheduler.every(10.seconds()).run(move || {
            match metrics.lock() {
                Ok(mut metrics) => {
                    let query_table_size: i128 = match query_entropy_metrics.lock() {
                        Ok(data) => data.len() as i128,
                        Err(e) => {
                            error!("Could not acquire query entropy mutex for metrics: {}", e);
                            -1
                        }
                    };

                    let response_table_size: i128 = match response_entropy_metrics.lock() {
                        Ok(data) => data.len() as i128,
                        Err(e) => {
                            error!("Could not acquire response entropy mutex for metrics: {}", e);
                            -1
                        }
                    };

                    metrics.set_gauge("processors.dns.entropy.queries.table_size", query_table_size);
                    metrics.set_gauge("processors.dns.entropy.responses.table_size", response_table_size);
                }, 
                Err(e) => error!("Could not acquire metrics mutex: {}", e)
            }
        });

        thread::spawn(move || {
            loop {
                scheduler.run_pending();
                thread::sleep(std::time::Duration::from_millis(10));
            }
        });
        
        DnsProcessor {
            system_state,
            dns_table,
            query_entropy,
            response_entropy,
            entropy_zscore_threshold: 3.0 // TODO
        }
    }

    pub fn process(&mut self, packet: &Arc<DNSPacket>) {
        match self.dns_table.lock() {
            Ok(mut table) => {
                match packet.dns_type {
                    DNSType::Query => {
                        debug!("DNS request: {:?}", packet);

                        // Walk all queries.
                        if let Some(queries) = &packet.queries {
                            for query in queries {
                                // Entropy.
                                if let Some(transaction_id) = packet.transaction_id { // Skip MDNS.
                                    if let Some(entropy) = query.entropy {
                                        // Add entropy to table.
                                        Self::record_entropy(&mut self.query_entropy, entropy);

                                        // Calculate zscore of entropy.
                                        let zscore = Self::entropy_zscore(&mut self.query_entropy, entropy);

                                        // Handle outlier if we have a zscore, training is over, and we are above threshold.
                                        if let Some(zscore) = zscore {
                                            if !self.system_state.is_in_training() &&  zscore.zscore > self.entropy_zscore_threshold {
                                                table.register_exceeded_entropy(
                                                    transaction_id,
                                                    entropy,
                                                    zscore.zscore,
                                                    zscore.mean
                                                );
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        table.register_request(packet);
                    },
                    DNSType::QueryResponse => {
                        debug!("DNS response: {:?}", packet);

                        // Walk all responses.
                        if let Some(responses) = &packet.responses {
                            for response in responses {
                                if response.value.is_none() {
                                    continue
                                }

                                // Entropy.
                                if let Some(transaction_id) = packet.transaction_id { // Skip MDNS.
                                    if let Some(entropy) = response.entropy {
                                        // Add entropy to table.
                                        Self::record_entropy(&mut self.response_entropy, entropy);

                                        // Calculate zscore of entropy if training period is over.
                                        let zscore = Self::entropy_zscore(&mut self.response_entropy, entropy);

                                        // Handle outlier if we have a zscore, training is over and we are above threshold.
                                        if let Some(zscore) = zscore {
                                            if !self.system_state.is_in_training() && zscore.zscore > self.entropy_zscore_threshold {
                                                table.register_exceeded_entropy(
                                                    transaction_id,
                                                    entropy,
                                                    zscore.zscore,
                                                    zscore.mean
                                                );
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        table.register_response(packet);
                    }
                }
            },
            Err(e) => {
                error!("Could not process DNS packet: {}", e);
            }
        }
    }

    
    fn record_entropy(table: &mut Arc<Mutex<HashMap<DateTime<Utc>, f32>>>, entropy: f32) {
        match table.lock() {
            Ok(mut table) => {
                table.insert(Utc::now(), entropy);
            }
            Err(e) => error!("Could not acquire DNS entropy table mutex: {}", e)
        }
    }

    #[allow(clippy::needless_return)]
    fn entropy_zscore(table: &mut Arc<Mutex<HashMap<DateTime<Utc>, f32>>>, entropy: f32)
        -> Option<ZScoreResult> {

        match table.lock() {
            Ok(table) => {
                let values: Vec<f32> = table.values().copied().collect();

                let mean = math::mean(&values);
                let stddev = math::std_deviation(&values);

                return match (mean, stddev) {
                    (Some(mean), Some(std_deviation)) => {
                        let diff = entropy - mean;
                        Some(ZScoreResult { zscore: diff / std_deviation, mean })
                    },
                    _ => None
                };
            },
            Err(e) => {
                error!("Could not acquire entropy table mutex for zscore calculation: {}", e);
                None
            }
        }
    }

    fn clean_entropy_table(table: &Arc<Mutex<HashMap<DateTime<Utc>, f32>>>) {
        match table.lock() {
            Ok(mut table) => {
                let mut new_table: HashMap<DateTime<Utc>, f32> = HashMap::new();

                #[allow(deprecated)] let cutoff = Utc::now() - Duration::minutes(10);
                for (dt, val) in &*table {
                    if *dt > cutoff {
                        new_table.insert(*dt, *val);
                    }
                }

                *table = new_table;
            },
            Err(e) => error!("Could not acquire entropy table mutex for retention cleaning: {}", e)
        }
    }

}
