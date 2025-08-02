use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use log::error;
use crate::link::leaderlink::Leaderlink;
use crate::metrics::Metrics;
use crate::wireless::positioning::gnss::gnss_constellation::GNSSConstellation;
use crate::wireless::positioning::nmea::nmea_sentences::{GPGGASentence, GPGSVSentence};

pub struct GnssMonitorTable {
    metrics: Arc<Mutex<Metrics>>,
    leaderlink: Arc<Mutex<Leaderlink>>,

    constellations: Mutex<HashMap<GNSSConstellation, GNSSConstellationData>>
}

pub struct GNSSConstellationData {
    maximum_time_deviation: i128,
    // lat, lon (dedup same)
    // min fix quality
    // min/max num of sats for fix
    // min/max altitude
    // fix_sats (their IDs)
    // min/max pdop (dop)
    // sats in view + details (dedup)
}

impl GnssMonitorTable {
    pub fn new(leaderlink: Arc<Mutex<Leaderlink>>, metrics: Arc<Mutex<Metrics>>) -> Self {
        GnssMonitorTable {
            leaderlink,
            metrics,
            constellations: Mutex::new(HashMap::new())
        }
    }

    /*
     * GPGGA contains:
     *  - Current time
     *  - Lat/Lon
     *  - Fix quality
     *  - Number of satellites used for fix
     *  - HDOP
     *  - Altitude
     *  - Geoid Separation
     */
    pub fn register_gpgga_sentence(&self, sentence: GPGGASentence) {

    }

    /*
     * GPGSA contains:
     *  - Selection mode (Automatic, Manual)
     *  - Fix type (None, 2D, 3D)
     *  - Satellites used for fix
     *  - PDOP, HDOP, VDOP  (Position, Horizontal, Vertical. Position is combined dilution.)
     */
    pub fn register_gpgsa_sentence(&self, sentence: GPGGASentence) {

    }

    /*
     * GPGSV contains:
     *  - Count of satellites in view
     *  - Details of each satellites in view
     *
     * The payload is often split over multiple messages to accommodate NMEA length limits.
     */
    pub fn register_gpgsv_sentence(&self, sentence: GPGSVSentence) {

    }

    pub fn calculate_metrics(&self) {
        error!("IMPLEMENT ME")
    }

    pub fn process_report(&self) {
        error!("IMPLEMENT ME")
    }

}