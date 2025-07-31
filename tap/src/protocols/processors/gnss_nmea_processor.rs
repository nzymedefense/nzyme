use std::sync::{Arc, Mutex};
use log::{error, info};
use crate::state::tables::gnss_table::GnssTable;
use crate::wireless::positioning::gnss::nmea_message::NmeaMessage;

pub struct GnssNmeaProcessor {
    gnss_table: Arc<Mutex<GnssTable>>,
}

impl GnssNmeaProcessor {

    pub fn new(gnss_table: Arc<Mutex<GnssTable>>) -> Self {
        Self { gnss_table }
    }

    pub fn process(&mut self, message: Arc<NmeaMessage>) {
        match self.gnss_table.lock() {
            Ok(table) => {
                // table.register_nmea_message

                info!("MESSAGE: {:?}", message)
            },
            Err(e) => {
                error!("Could not acquire GNSS table: {}", e);
            }
        }
    }

}