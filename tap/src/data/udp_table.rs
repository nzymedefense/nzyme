use std::sync::{Arc, Mutex};
use log::info;
use crate::ethernet::packets::Datagram;
use crate::link::leaderlink::Leaderlink;
use crate::metrics::Metrics;

pub struct UdpTable {
    leaderlink: Arc<Mutex<Leaderlink>>,
    metrics: Arc<Mutex<Metrics>>
}

impl UdpTable {

    pub fn new(leaderlink: Arc<Mutex<Leaderlink>>, metrics: Arc<Mutex<Metrics>>) -> Self {
        Self { leaderlink, metrics }
    }

    pub fn register_datagram(&mut self, datagram: &Arc<Datagram>) {
        info!("DATAGRAM")
    }

}