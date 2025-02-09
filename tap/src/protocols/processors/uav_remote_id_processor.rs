use std::sync::{Arc, Mutex};
use log::error;
use crate::protocols::detection::taggers::remoteid::messages::UavRemoteIdMessage;
use crate::state::tables::uav_table::UavTable;

pub struct UavRemoteIdProcessor {
    uav_table: Arc<Mutex<UavTable>>,
}

impl UavRemoteIdProcessor {
    pub fn new(uav_table: Arc<Mutex<UavTable>>) -> Self {
        Self { uav_table }
    }

    pub fn process(&mut self, message: Arc<UavRemoteIdMessage>) {
        match self.uav_table.lock() {
            Ok(mut table) => table.register_remote_id_message(message),
            Err(e) => {
                error!("Could not acquire UAV table: {}", e);
            }
        }
    }

}