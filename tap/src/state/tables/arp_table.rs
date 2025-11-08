use std::collections::HashMap;
use std::net::IpAddr;
use std::sync::{Arc, Mutex};
use chrono::{DateTime, Duration, Utc};
use clokwerk::{Scheduler, TimeUnits};
use log::{error, info};
use crate::alerting::alert_types::{AlertAttribute, ArpAlert};
use crate::alerting::alert_types::ArpAlertType::PoisoningDetected;
use crate::helpers::timer::{record_timer, Timer};
use crate::link::leaderlink::Leaderlink;
use crate::link::reports::arp_packets_report;
use crate::metrics::Metrics;
use crate::state::tables::table_helpers::clear_mutex_vector;
use crate::wired::packets::ArpPacket;
use crate::wired::types::ArpOpCode;

pub struct ArpTable {
    leaderlink: Arc<Mutex<Leaderlink>>,
    metrics: Arc<Mutex<Metrics>>,
    requests: Mutex<Vec<Arc<ArpPacket>>>,
    replies: Mutex<Vec<Arc<ArpPacket>>>,
    ip_mappings: Arc<Mutex<HashMap<IpAddr, Vec<ClaimedMacAddress>>>>,
    poisoning_monitor: bool,
    poisoning_window: Duration,
    alerts: Mutex<Vec<ArpAlert>>
}

struct ClaimedMacAddress {
    address: String,
    last_seen: DateTime<Utc>
}

impl ArpTable {

    pub fn new(leaderlink: Arc<Mutex<Leaderlink>>,
               metrics: Arc<Mutex<Metrics>>,
               poisoning_monitor: bool,
               poisoning_window_seconds: i32) -> Self {
        let ip_mappings = Arc::new(Mutex::new(HashMap::new()));

        let scheduler_mappings = ip_mappings.clone();
        let mut scheduler = Scheduler::new();
        scheduler.every(5.minutes()).run(move || {
            Self::clean_mappings(&scheduler_mappings);
        });

        ArpTable {
            leaderlink,
            metrics,
            requests: Mutex::new(Vec::new()),
            replies: Mutex::new(Vec::new()),
            ip_mappings,
            poisoning_monitor,
            poisoning_window: Duration::seconds(poisoning_window_seconds as i64),
            alerts: Mutex::new(Vec::new())
        }
    }

    pub fn register_request(&self, packet: Arc<ArpPacket>) {
        if !packet.operation.eq(&ArpOpCode::Request) {
            error!("Attempt to register ARP request packet with wrong OpCode.");
            return
        }

        match self.requests.lock() {
            Ok(mut requests) => {
                requests.push(packet.clone());
            },
            Err(e) => error!("Could not acquire ARP request table mutex: {}", e)
        }

        self.track_ip_mapping_and_detect_flap(&packet);
    }

    pub fn register_reply(&self, packet: Arc<ArpPacket>) {
        if !packet.operation.eq(&ArpOpCode::Reply) {
            error!("Attempt to register ARP reply packet with wrong OpCode");
            return
        }

        match self.replies.lock() {
            Ok(mut replies) => {
                replies.push(packet.clone());
            },
            Err(e) => error!("Could not acquire ARP replies table mutex: {}", e)
        }

        self.track_ip_mapping_and_detect_flap(&packet);
    }

    fn track_ip_mapping_and_detect_flap(&self, packet: &ArpPacket) {
        if !self.poisoning_monitor {
            return
        }

        let spa = packet.arp_sender_address;
        let sha = &packet.arp_sender_mac;
        let now = packet.timestamp;

        if spa.is_unspecified() {
            // Don't consider 0.0.0.0.
            return
        }

        match self.ip_mappings.lock() {
            Ok(mut map) => {
                let entry = map.entry(spa).or_insert_with(Vec::new);

                // Is this MAC already in the list?
                if let Some(rec) = entry.iter_mut().find(|r| r.address == *sha) {
                    // Yes, update timestamp.
                    rec.last_seen = now;
                    return;
                }

                /*
                 * New MAC for this IP. Check existing ones for recent sightings.
                 * We assume that there should never be two different MACs in the flap
                 * window.
                 */
                let has_recent_other = entry.iter()
                    .any(|r| now - r.last_seen <= self.poisoning_window);

                // Insert the new MAC record.
                entry.push(ClaimedMacAddress {
                    address: sha.clone(),
                    last_seen: now,
                });

                // Alert if another MAC was seen very recently.
                if has_recent_other {
                    // TODO TODO TODO
                    info!("ARP FLAP detected: IP {} changed to MAC {} within {}s", spa, sha, self.poisoning_window.num_seconds());

                    match self.alerts.lock() {
                        Ok(mut alerts) => {
                            let mut attributes: HashMap<String, AlertAttribute> = HashMap::new();
                            attributes.insert("ip_address".to_string(), AlertAttribute::String(spa.to_string()));
                            attributes.insert("mac_address".to_string(), AlertAttribute::String(sha.clone()));

                            alerts.push(ArpAlert {
                                alert_type: PoisoningDetected,
                                attributes
                            });
                        },
                        Err(e) => error!("Could not acquire ARP alerts mutex: {}", e)
                    }
                }
            }
            Err(e) => error!("Could not acquire IP mappings mutex: {}", e),
        }
    }

    pub fn process_report(&self) {
        let mut timer = Timer::new();

        // Combine replies and responses into single report and transmit.
        let mut merged_packets: Vec<Arc<ArpPacket>> = Vec::new();

        match self.requests.lock() {
            Ok(r) => merged_packets.append(&mut r.clone()),
            Err(e) => {
                error!("Could not acquire requests mutex to build ARP report: {}", e);
            }
        }

        match self.replies.lock() {
            Ok(r) => merged_packets.append(&mut r.clone()),
            Err(e) => {
                error!("Could not acquire replies mutex to build ARP report: {}", e);
            }
        }

        // Generate JSON.
        let report = match serde_json::to_string(&arp_packets_report::generate(&merged_packets)) {
            Ok(report) => report,
            Err(e) => {
                error!("Could not serialize ARP packets report: {}", e);
                return;
            }
        };
        timer.stop();
        record_timer(
            timer.elapsed_microseconds(),
            "tables.arp.timer.report_generation",
            &self.metrics
        );

        // Send report.
        match self.leaderlink.lock() {
            Ok(link) => {
                if let Err(e) = link.send_report("arp/packets", report) {
                    error!("Could not submit ARP packets report: {}", e);
                }
            },
            Err(e) => error!("Could not acquire leader link lock for ARP packets \
                        report submission: {}", e)
        }

        clear_mutex_vector(&self.requests);
        clear_mutex_vector(&self.replies);

        clear_mutex_vector(&self.alerts);
    }

    pub fn calculate_metrics(&self) {
        let requests_table_size: i128 = match self.requests.lock() {
            Ok(r) => r.len() as i128,
            Err(e) => {
                error!("Could not acquire mutex to calculate ARP requests table size: {}", e);

                -1
            }
        };

        let replies_table_size: i128 = match self.replies.lock() {
            Ok(r) => r.len() as i128,
            Err(e) => {
                error!("Could not acquire mutex to calculate ARP replies table size: {}", e);

                -1
            }
       };

        let ip_mappings_size: i128 = match self.ip_mappings.lock() {
            Ok(map) => map.values().map(|vec| vec.len() as i128).sum::<i128>(),
            Err(e) => {
                error!("Could not acquire IP mappings mutex: {}", e);

                -1
            }
        };

        match self.metrics.lock() {
            Ok(mut metrics) => {
                metrics.set_gauge("tables.arp.requests.size", requests_table_size);
                metrics.set_gauge("tables.arp.replies.size", replies_table_size);
                metrics.set_gauge("tables.arp.ip_mappings.size", ip_mappings_size);
            },
            Err(e) => error!("Could not acquire metrics mutex: {}", e)
        }
    }

    fn clean_mappings(ip_mappings: &Arc<Mutex<HashMap<IpAddr, Vec<ClaimedMacAddress>>>>) {
        let cutoff = Utc::now() - Duration::minutes(10);

        match ip_mappings.lock() {
            Ok(mut map) => {
                map.retain(|_, macs| {
                    macs.retain(|c| c.last_seen >= cutoff);
                    !macs.is_empty()
                });
            }
            Err(e) => {
                error!("Could not acquire IP mappings mutex: {}", e);
            }
        }
    }

}