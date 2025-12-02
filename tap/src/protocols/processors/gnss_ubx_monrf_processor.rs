use std::sync::{Arc, Mutex};
use log::{error};
use crate::state::tables::gnss_monitor_table::GnssMonitorTable;
use crate::wireless::positioning::axia::ubx::UbxMonRfMessage;

pub struct GnssUbxMonRfProcessor {
    gnss_monitor_table: Arc<Mutex<GnssMonitorTable>>
}

impl GnssUbxMonRfProcessor {

    pub fn new(gnss_monitor_table: Arc<Mutex<GnssMonitorTable>>) -> Self {
        Self { gnss_monitor_table }
    }

    pub fn process(&mut self, message: Arc<UbxMonRfMessage>) {
        match self.gnss_monitor_table.lock() {
            Ok(mut gnss_monitor_table) => {
                gnss_monitor_table.register_ubx_monrf_message(message);
            },
            Err(e) => {
                error!("Could not acquire GNSS table: {}", e);
            }
        }
    }
}