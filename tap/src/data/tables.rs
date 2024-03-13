use std::{
    collections::HashMap,
    sync::{Arc, Mutex}, thread
};
use std::path::PathBuf;

use log::{error};
use std::time::Duration;
use crate::data::tcp_table::TcpTable;

use crate::metrics::Metrics;

use super::{dns_table::DnsTable, dot11_table::Dot11Table};

pub struct Tables {
    pub arp: Arc<Mutex<HashMap<String, HashMap<String, u128>>>>,
    pub dns: Arc<Mutex<DnsTable>>,
    pub dot11: Arc<Mutex<Dot11Table>>,
    pub tcp: Arc<Mutex<TcpTable>>
}

impl Tables {

    pub fn new(metrics: Arc<Mutex<Metrics>>, data_directory: Arc<PathBuf>) -> Self {
        Tables {
            arp: Arc::new(Mutex::new(HashMap::new())),
            dns: Arc::new(Mutex::new(DnsTable::new(metrics))),
            dot11: Arc::new(Mutex::new(Dot11Table::new())),
            tcp: Arc::new(Mutex::new(TcpTable::new(data_directory)))
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
            Ok(dns) => dns.calculate_metrics(),
            Err(e) => error!("Could not acquire mutex to calculate DNS metrics: {}", e)
        }
    }

    pub fn pre_transmission(&self) {
        match self.tcp.lock() {
            Ok(tcp) => tcp.pre_transmission(),
            Err(e) => error!("Could not acquire mutex to execute TCP pre transmission jobs: {}", e)
        }
    }

    pub fn post_transmission(&self) {
        match self.dns.lock() {
            Ok(dns) => dns.post_transmission(),
            Err(e) => error!("Could not acquire mutex to clear DNS table: {}", e)
        }

        match self.dot11.lock() {
            Ok(mut dot11) => dot11.post_transmission(),
            Err(e) => error!("Could not acquire mutex to clear 802.11 table: {}", e)
        }
    }

}