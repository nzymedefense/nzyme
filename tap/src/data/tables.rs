use std::{
    collections::HashMap,
    sync::{Arc, Mutex}, thread
};

use log::error;
use std::time::Duration;

use crate::metrics::Metrics;

use super::{dns_table::DnsTable, l4_table::L4Table, dot11_networks_table::Dot11NetworksTable};

pub struct Tables {
    pub arp: Arc<Mutex<HashMap<String, HashMap<String, u128>>>>,
    pub dns: Arc<Mutex<DnsTable>>,
    pub l4: Arc<Mutex<L4Table>>,
    pub dot11_networks: Arc<Mutex<Dot11NetworksTable>>
}

impl Tables {

    pub fn new(metrics: Arc<Mutex<Metrics>>) -> Self {       
        Tables {
            arp: Arc::new(Mutex::new(HashMap::new())),
            dns: Arc::new(Mutex::new(DnsTable::new(metrics))),
            l4: Arc::new(Mutex::new(L4Table::new())),
            dot11_networks: Arc::new(Mutex::new(Dot11NetworksTable::new()))
        }
    }

    pub fn run_background_jobs(&self) {
        loop {
            self.calculate_metrics();
            thread::sleep(Duration::from_secs(10));
        }
    }

    pub fn calculate_metrics(&self) {
        match self.dns.lock() {
            Ok(dns) => {
                dns.calculate_metrics();
            },
            Err(e) => {
                error!("Could not acquire mutex to calculate DNS metrics: {}", e);
            }
        }
    }

    pub fn clear_ephemeral(&self) {
        match self.dns.lock() {
            Ok(dns) => {
                dns.clear_ephemeral();
            },
            Err(e) => {
                error!("Could not acquire mutex to clear DNS table: {}", e);
            }
        }
        
        match self.l4.lock() {
            Ok(mut l4) => {
                l4.clear_ephemeral();
            },
            Err(e) => {
                error!("Could not acquire mutex to clear L4 table: {}", e);
            }
        }
    }

}