use std::sync::{Arc, Mutex};
use log::error;
use crate::link::leaderlink::Leaderlink;
use crate::metrics::Metrics;

pub struct GnssTable {
    metrics: Arc<Mutex<Metrics>>,
    leaderlink: Arc<Mutex<Leaderlink>>,
}

impl GnssTable {
    pub fn new(leaderlink: Arc<Mutex<Leaderlink>>, metrics: Arc<Mutex<Metrics>>) -> Self {
        GnssTable {
            leaderlink,
            metrics
        }
    }

    pub fn calculate_metrics(&self) {
        error!("IMPLEMENT ME")
    }

    pub fn process_report(&self) {
        error!("IMPLEMENT ME")
    }

}