use std::collections::HashMap;
use std::net::IpAddr;
use std::sync::{Arc, Mutex};
use chrono::{DateTime, Duration, Utc};
use log::error;
use crate::metrics::Metrics;

pub struct ArpState {
    mapping: HashMap<String, Vec<ArpIpAddr>>,
    metrics: Arc<Mutex<Metrics>>
}

#[derive(Debug, Clone)]
pub struct ArpIpAddr {
    pub addr: IpAddr,
    pub last_seen: DateTime<Utc>
}

impl ArpState {

    pub fn new(metrics: Arc<Mutex<Metrics>>) -> Self {
        Self {
            mapping: HashMap::new(),
            metrics
        }
    }

    pub fn update(&mut self, mac: String, ip: IpAddr) {
        let entry = self.mapping
            .entry(mac)
            .or_default();

        if let Some(arp_ip) = entry.iter_mut().find(|arp_ip| arp_ip.addr == ip) {
            // IP address already exists, update last_seen.
            arp_ip.last_seen = Utc::now();
        } else {
            // New IP address.
            entry.push(ArpIpAddr {
                addr: ip,
                last_seen: Utc::now(),
            });
        }
    }

    pub fn get_mapping(&self) -> HashMap<String, Vec<ArpIpAddr>> {
        self.mapping.clone()
    }

    pub fn mac_address_of_ip_address(&self, address: IpAddr) -> Option<String> {
        for (mac, vec) in &self.mapping {
            if vec.iter().any(|arp_ip| arp_ip.addr == address) {
                return Some(mac.clone());
            }
        }

        None
    }

    pub fn calculate_metrics(&self) {
        match self.metrics.lock() {
            Ok(mut metrics) => {
                metrics.set_gauge("state.arp.macs.size", self.mapping.len() as i128);
                metrics.set_gauge(
                    "state.arp.ips.size",
                    self.mapping.values().map(|vec| vec.len() as i128).sum()
                );

            },
            Err(e) => error!("Could not acquire metrics mutex: {}", e)
        }
    }

    pub fn retention_clean(&mut self) {
        let expiration_time = Utc::now() - Duration::try_seconds(300).unwrap();

        self.mapping.retain(|_, vec| {
            vec.retain(|arp_ip| arp_ip.last_seen > expiration_time);

            // Return true to keep the entry in the map if it still has any ArpIpAddr items.
            !vec.is_empty()
        });
    }

}