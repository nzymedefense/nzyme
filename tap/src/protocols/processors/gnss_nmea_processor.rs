use std::panic;
use std::sync::{Arc, Mutex};
use log::{debug, error, trace};
use crate::protocols::parsers::nmea_parser::{parse_gga, parse_gsa, parse_gsv};
use crate::state::tables::gnss_monitor_table::GnssMonitorTable;
use crate::wireless::positioning::nmea::nmea_message::NMEAMessage;
use crate::wireless::positioning::nmea::nmea_sentence_types::NMEASentenceType;

pub struct GnssNmeaProcessor {
    gnss_monitor_table: Arc<Mutex<GnssMonitorTable>>
}

impl GnssNmeaProcessor {

    pub fn new(gnss_monitor_table: Arc<Mutex<GnssMonitorTable>>) -> Self {
        Self { gnss_monitor_table }
    }

    pub fn process(&mut self, message: Arc<NMEAMessage>) {
        /*
         * We are catching potential panics just in case of parser issues with potentially
         * corrupted NMEA data.
         */
        if let Err(e) = panic::catch_unwind(||
            match self.gnss_monitor_table.lock() {
                Ok(mut gnss_monitor_table) => {
                    if message.sentence.len() < 7 {
                        error!("NMEA sentence is too short: {}", message.sentence);
                        return;
                    }

                    // Determine sentence type.
                    let sentence_type: NMEASentenceType = match
                            NMEASentenceType::try_from(&message.sentence[3..6]) {
                        Ok(sentence_type) => sentence_type,
                        Err(e) => {
                            debug!("Failed to parse NMEA type from sentence: [{}]", message.sentence);
                            return;
                        }
                    };

                    match sentence_type {
                        NMEASentenceType::GGA => {
                            match parse_gga(&message) {
                                Ok(sentence) => {
                                    trace!("NMEA GGA sentence: {:?}", sentence);
                                    gnss_monitor_table.register_gga_sentence(sentence);
                                },
                                Err(e) => {
                                    error!("Failed to parse NMEA GGA message: {}", e);
                                }
                            }
                        }
                        NMEASentenceType::GSA => {
                            match parse_gsa(&message) {
                                Ok(sentence) => {
                                    trace!("NMEA GSA sentence: {:?}", sentence);
                                    gnss_monitor_table.register_gsa_sentence(sentence);
                                },
                                Err(e) => {
                                    error!("Failed to parse NMEA GSA message: {}", e);
                                }
                            }
                        }
                        NMEASentenceType::GSV => {
                            match parse_gsv(&message) {
                                Ok(sentence) => {
                                    trace!("NMEA GSV sentence: {:?}", sentence);
                                    gnss_monitor_table.register_gsv_sentence(sentence);
                                },
                                Err(e) => {
                                    error!("Failed to parse NMEA GSV message: {}", e);
                                }
                            }
                        }
                    }
                },
                Err(e) => {
                    error!("Could not acquire GNSS table: {}", e);
                }
            }
        ) {
            error!("GNSS processor thread panic: {:?}", e);
        }
    }

}