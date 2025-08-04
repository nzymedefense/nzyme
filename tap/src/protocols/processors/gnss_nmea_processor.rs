use std::sync::{Arc, Mutex};
use log::{debug, error, info, trace};
use crate::protocols::parsers::nmea_parser::{parse_gpgga, parse_gpgsa, parse_gpgsv};
use crate::state::tables::gnss_monitor_table::GnssMonitorTable;
use crate::wireless::positioning::nmea::nmea_message::NMEAMessage;
use crate::wireless::positioning::nmea::nmea_sentence_types::NMEASentenceType;

pub struct GnssNmeaProcessor {
    gnss_monitor_table: Arc<Mutex<GnssMonitorTable>>,
}

impl GnssNmeaProcessor {

    pub fn new(gnss_monitor_table: Arc<Mutex<GnssMonitorTable>>) -> Self {
        Self { gnss_monitor_table }
    }

    pub fn process(&mut self, message: Arc<NMEAMessage>) {
        match self.gnss_monitor_table.lock() {
            Ok(mut gnss_monitor_table) => {
                // Determine sentence type.
                let sentence_type: NMEASentenceType = match
                        NMEASentenceType::try_from(&message.sentence[1..6]) {
                    Ok(sentence_type) => sentence_type,
                    Err(e) => {
                        debug!("Failed to parse NMEA type from sentence: [{}]", message.sentence);
                        return;
                    }
                };

                match sentence_type {
                    NMEASentenceType::GPGGA => {
                        match parse_gpgga(&message) {
                            Ok(sentence) => {
                                trace!("NMEA GPGGA sentence: {:?}", sentence);
                                gnss_monitor_table.register_gpgga_sentence(sentence);
                            },
                            Err(e) => {
                                error!("Failed to parse NMEA GPGGA message: {}", e);
                            }
                        }
                    }
                    NMEASentenceType::GPGSA => {
                        match parse_gpgsa(&message) {
                            Ok(sentence) => {
                                trace!("NMEA GPGSA sentence: {:?}", sentence);
                                gnss_monitor_table.register_gpgsa_sentence(sentence);
                            },
                            Err(e) => {
                                error!("Failed to parse NMEA GPGSA message: {}", e);
                            }
                        }
                    }
                    NMEASentenceType::GPGSV => {
                        match parse_gpgsv(&message) {
                            Ok(sentence) => {
                                trace!("NMEA GPGSV sentence: {:?}", sentence);
                                gnss_monitor_table.register_gpgsv_sentence(sentence);
                            },
                            Err(e) => {
                                error!("Failed to parse NMEA GPGSV message: {}", e);
                            }
                        }
                    }
                }
            },
            Err(e) => {
                error!("Could not acquire GNSS table: {}", e);
            }
        }
    }

}