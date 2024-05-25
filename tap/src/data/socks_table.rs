use std::sync::{Arc, Mutex};
use log::error;
use crate::data::table_helpers::clear_mutex_vector;
use crate::ethernet::packets::SocksTunnel;
use crate::helpers::timer::{record_timer, Timer};
use crate::link::leaderlink::Leaderlink;
use crate::link::reports::socks_tunnels_report;
use crate::metrics::Metrics;

pub struct SocksTable {
    leaderlink: Arc<Mutex<Leaderlink>>,
    metrics: Arc<Mutex<Metrics>>,
    tunnels: Mutex<Vec<Arc<SocksTunnel>>>
}

impl SocksTable {

    pub fn new(leaderlink: Arc<Mutex<Leaderlink>>, metrics: Arc<Mutex<Metrics>>) -> Self {
        Self {
            leaderlink,
            metrics,
            tunnels: Mutex::new(Vec::new())
        }
    }

    pub fn register_tunnel(&mut self, tunnel: Arc<SocksTunnel>) {
        match self.tunnels.lock() {
            Ok(mut tunnels) => tunnels.push(tunnel.clone()),
            Err(e) => error!("Could not acquired SOCKS tunnels table mutex: {}", e)
        }
    }

    pub fn process_report(&self) {
        match self.tunnels.lock() {
            Ok(tunnels) => {
                // Generate JSON.
                let mut timer = Timer::new();
                let report = match serde_json::to_string(&socks_tunnels_report::generate(&tunnels)) {
                    Ok(report) => report,
                    Err(e) => {
                        error!("Could not serialize SOCKS tunnels report: {}", e);
                        return;
                    }
                };
                timer.stop();
                record_timer(
                    timer.elapsed_microseconds(),
                    "tables.socks.timer.report_generation",
                    &self.metrics
                );

                // Send report.
                match self.leaderlink.lock() {
                    Ok(link) => {
                        if let Err(e) = link.send_report("socks/tunnels", report) {
                            error!("Could not submit SOCKS tunnels report: {}", e);
                        }
                    },
                    Err(e) => error!("Could not acquire leader link lock for SOCKS tunnels \
                                        report submission: {}", e)
                }
            },
            Err(e) => {
                error!("Could not acquire SOCKS tunnels table mutex for report generation: {}", e);
            }
        }

        // Clean up.
        clear_mutex_vector(&self.tunnels);
    }

    pub fn calculate_metrics(&self) {
        let tunnels_size: i128 = match self.tunnels.lock() {
            Ok(d) => d.len() as i128,
            Err(e) => {
                error!("Could not acquire mutex to calculate SOCKS tunnels table size: {}", e);

                -1
            }
        };

        match self.metrics.lock() {
            Ok(mut metrics) => {
                metrics.set_gauge("tables.socks.tunnels.size", tunnels_size);
            },
            Err(e) => error!("Could not acquire metrics mutex: {}", e)
        }
    }

}