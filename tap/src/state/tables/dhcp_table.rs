use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use log::info;
use crate::link::leaderlink::Leaderlink;
use crate::metrics::Metrics;
use crate::wired::packets::{Dhcpv4Packet, Dhcpv4Transaction};

pub struct DhcpTable {
    leaderlink: Arc<Mutex<Leaderlink>>,
    metrics: Arc<Mutex<Metrics>>,
    macs4: Mutex<HashMap<String, Dhcpv4Transaction>>
}

impl DhcpTable {

    pub fn new(leaderlink: Arc<Mutex<Leaderlink>>, metrics: Arc<Mutex<Metrics>>) -> Self {
        DhcpTable {
            leaderlink,
            metrics,
            macs4: Mutex::new(HashMap::new())
        }
    }

    pub fn register_dhcpv4_packet(&self, dhcp: Arc<Dhcpv4Packet>) {
        info!("DHCP: {:?}", dhcp)
    }

    pub fn process_report(&self) {
        // TODO
    }

    pub fn calculate_metrics(&self) {
        // TODO
    }

}