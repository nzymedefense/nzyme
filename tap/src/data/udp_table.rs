use std::sync::{Arc, Mutex};
use log::error;
use crate::data::table_helpers::clear_mutex_vector;
use crate::ethernet::packets::Datagram;
use crate::helpers::timer::{record_timer, Timer};
use crate::link::leaderlink::Leaderlink;
use crate::link::reports::{tcp_sessions_report, udp_datagrams_report};
use crate::metrics::Metrics;

pub struct UdpTable {
    leaderlink: Arc<Mutex<Leaderlink>>,
    metrics: Arc<Mutex<Metrics>>,
    datagrams: Mutex<Vec<Arc<Datagram>>>
}

impl UdpTable {

    pub fn new(leaderlink: Arc<Mutex<Leaderlink>>, metrics: Arc<Mutex<Metrics>>) -> Self {
        Self {
            leaderlink,
            metrics,
            datagrams: Mutex::new(Vec::new())
        }
    }

    pub fn register_datagram(&mut self, datagram: &Arc<Datagram>) {
        match self.datagrams.lock() {
            Ok(mut datagrams) => datagrams.push((*datagram).clone()),
            Err(e) => {
                error!("Could not acquire datagram table mutex: {}", e);
            }
        }
    }

    pub fn process_report(&self) {
        match self.datagrams.lock() {
            Ok(datagrams) => {
                // Generate JSON.
                let mut timer = Timer::new();
                let report = match serde_json::to_string(&udp_datagrams_report::generate(&datagrams)) {
                    Ok(report) => report,
                    Err(e) => {
                        error!("Could not serialize UDP datagrams report: {}", e);
                        return;
                    }
                };
                timer.stop();
                record_timer(
                    timer.elapsed_microseconds(),
                    "tables.udp.timer.report_generation",
                    &self.metrics
                );

                // Send report.
                match self.leaderlink.lock() {
                    Ok(link) => {
                        if let Err(e) = link.send_report("udp/datagrams", report) {
                            error!("Could not submit UDP datagrams report: {}", e);
                        }
                    },
                    Err(e) => error!("Could not acquire leader link lock for UDP report submission: {}", e)
                }
            },
            Err(e) => {
                error!("Could not acquire UDP datagrams table mutex for report generation: {}", e);
            }
        }
        
        // Clean up.
        clear_mutex_vector(&self.datagrams);
    }

    pub fn calculate_metrics(&self) {
        let datagrams_size: i128 = match self.datagrams.lock() {
            Ok(d) => d.len() as i128,
            Err(e) => {
                error!("Could not acquire mutex to calculate datagram table size: {}", e);

                -1
            }
        };

        match self.metrics.lock() {
            Ok(mut metrics) => {
                metrics.set_gauge("tables.udp.datagrams.size", datagrams_size);
            },
            Err(e) => error!("Could not acquire metrics mutex: {}", e)
        }
    }

}