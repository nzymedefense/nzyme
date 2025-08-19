use std::collections::{HashMap, HashSet};
use std::sync::{Arc, Mutex, MutexGuard};
use anyhow::Error;
use chrono::Utc;
use log::{error, info};
use crate::helpers::timer::{record_timer, Timer};
use crate::link::leaderlink::Leaderlink;
use crate::link::reports::gnss_constellations_report;
use crate::metrics::Metrics;
use crate::state::tables::table_helpers::clear_mutex_hashmap;
use crate::wireless::positioning::gnss::gnss_constellation::GNSSConstellation;
use crate::wireless::positioning::nmea::nmea_sentences::{FixType, GGASentence, GSASentence, GSVSentence};

pub struct GnssMonitorTable {
    metrics: Arc<Mutex<Metrics>>,
    leaderlink: Arc<Mutex<Leaderlink>>,

    constellations: Mutex<HashMap<GNSSConstellation, GNSSConstellationData>>
}

#[derive(Debug, Default)]
pub struct GNSSConstellationData {
    pub fixes: HashSet<FixType>,
    pub maximum_time_deviation_ms: Option<i64>,
    pub positions: Vec<LatLon>,
    pub maximum_fix_satellite_count: Option<u8>,
    pub minimum_fix_satellite_count: Option<u8>,
    pub fix_satellites: HashSet<u8>,
    pub maximum_altitude_meters: Option<f32>,
    pub minimum_altitude_meters: Option<f32>,
    pub maximum_pdop: Option<f32>,
    pub minimum_pdop: Option<f32>,
    pub satellites_in_view: HashMap<u8, SatelliteInfo>,
    pub minimum_satellites_in_view_count: Option<u8>,
    pub maximum_satellites_in_view_count: Option<u8>
}

impl GNSSConstellationData {
    pub fn insert_position_if_unique(&mut self, new_pos: LatLon) {
        if !self.positions.iter().any(|p| p.approx_eq(&new_pos)) {
            self.positions.push(new_pos);
        }
    }
}

#[derive(Debug, Default, PartialEq)]
pub struct LatLon {
    pub lat: f64,
    pub lon: f64
}

impl LatLon {
    fn approx_eq(&self, other: &LatLon) -> bool {
        const EPSILON: f64 = 1e-6;
        (self.lat - other.lat).abs() < EPSILON &&
            (self.lon - other.lon).abs() < EPSILON
    }
}

#[derive(Debug, Default, Eq, PartialEq)]
pub struct SatelliteInfo {
    pub prn: u8,
    pub elevation_degrees: Option<u8>,
    pub azimuth_degrees: Option<u16>,
    pub snr: Option<u8>,
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
     * GGA contains:
     *  - Current time
     *  - Lat/Lon
     *  - Fix quality (we ignore this and use DOP values for fix quality instead)
     *  - Number of satellites used for fix
     *  - HDOP
     *  - Altitude
     *  - Geoid Separation
     */
    pub fn register_gga_sentence(&mut self, sentence: GGASentence) {

        match self.constellations.lock() {
            Ok(mut constellations) => {
                if let Err(e) = Self::ensure_constellation(&sentence.constellation, &mut constellations) {
                    error!("Could not ensure constellation: {}", e);
                };

                let constellation = constellations.get_mut(&sentence.constellation).unwrap();

                // Maximum time deviation.
                let time_delta_ms = sentence.time
                    .map(|gps_time| sentence.timestamp
                        .signed_duration_since(gps_time)
                        .num_milliseconds()
                    );
                if let Some(time_delta) = time_delta_ms {
                    match constellation.maximum_time_deviation_ms {
                        Some(mtd) => {
                            if time_delta > mtd {
                                // Time delta is larger than previous max.
                                constellation.maximum_time_deviation_ms = Some(time_delta)
                            }
                        },
                        None => {
                            // First recorded time deviation.
                            constellation.maximum_time_deviation_ms = Some(time_delta)
                        }
                    };
                }

                // Latitude/Longitude. We dedup values that are equal.
                if let Some(lat) = sentence.latitude && let Some(lon) = sentence.longitude {
                    constellation.insert_position_if_unique(LatLon { lat, lon });
                }

                if let Some(num_satellites) = sentence.num_satellites {
                    // Maximum fix satellite count.
                    match constellation.maximum_fix_satellite_count {
                        Some(sc) => {
                            if num_satellites > sc {
                                // More sats than previous max.
                                constellation.maximum_fix_satellite_count = Some(num_satellites)
                            }
                        },
                        None => {
                            // First recorded max sats.
                            constellation.maximum_fix_satellite_count = Some(num_satellites)
                        }
                    };

                    // Minimum fix satellite count.
                    match constellation.minimum_fix_satellite_count {
                        Some(sc) => {
                            if num_satellites < sc {
                                // Less sats than previous min.
                                constellation.minimum_fix_satellite_count = Some(num_satellites)
                            }
                        },
                        None => {
                            // First recorded min sats.
                            constellation.minimum_fix_satellite_count = Some(num_satellites)
                        }
                    };
                }

                // Maximum altitude.
                if let Some(altitude) = sentence.altitude_m {
                    match constellation.maximum_altitude_meters {
                        Some(a) => {
                            if altitude > a {
                                // Altitude greater than previous max.
                                constellation.maximum_altitude_meters = Some(altitude)
                            }
                        },
                        None => {
                            // First recorded max altitude.
                            constellation.maximum_altitude_meters = Some(altitude)
                        }
                    };

                    // Minimum altitude.
                    match constellation.minimum_altitude_meters {
                        Some(a) => {
                            if altitude < a {
                                // Altitude less than previous min.
                                constellation.minimum_altitude_meters = Some(altitude)
                            }
                        },
                        None => {
                            // First recorded min altitude.
                            constellation.minimum_altitude_meters = Some(altitude);
                        }
                    };
                }
            },
            Err(e) => error!("Could not acquire constellations lock: {}", e)
        }
    }

    /*
     * GSA contains:
     *  - Selection mode (Automatic, Manual)
     *  - Fix type (None, 2D, 3D)
     *  - Satellites used for fix
     *  - PDOP, HDOP, VDOP  (Position, Horizontal, Vertical. Position is combined dilution.)
     */
    pub fn register_gsa_sentence(&mut self, sentence: GSASentence) {
        match self.constellations.lock() {
            Ok(mut constellations) => {
                if let Err(e) = Self::ensure_constellation(&sentence.constellation, &mut constellations) {
                    error!("Could not ensure constellation: {}", e);
                }

                let constellation = constellations.get_mut(&sentence.constellation).unwrap();

                constellation.fixes.insert(sentence.fix);
                constellation.fix_satellites.extend(sentence.fix_satellites);

                if let Some(pdop) = sentence.pdop {
                    // Maximum PDOP.
                    match constellation.maximum_pdop {
                        Some(p) => {
                            if pdop > p {
                                // PDOP is larger than previous max.
                                constellation.maximum_pdop = Some(pdop)
                            }
                        },
                        None => {
                            // First recorded maximum PDOP.
                            constellation.maximum_pdop = Some(pdop)
                        }
                    };

                    // Minimum PDOP.
                    match constellation.minimum_pdop {
                        Some(p) => {
                            if pdop < p {
                                // PDOP is less than previous min.
                                constellation.minimum_pdop = Some(pdop)
                            }
                        },
                        None => {
                            // First recorded minimum PDOP.
                            constellation.minimum_pdop = Some(pdop)
                        }
                    };
                }
            },
            Err(e) => error!("Could not acquire constellations lock: {}", e)
        }
    }

    /*
     * GSV contains:
     *  - Count of satellites in view
     *  - Details of each satellites in view
     *
     * The payload is often split over multiple messages to accommodate NMEA length limits.
     */
    pub fn register_gsv_sentence(&mut self, sentence: GSVSentence) {
        match self.constellations.lock() {
            Ok(mut constellations) => {
                if let Err(e) = Self::ensure_constellation(&sentence.constellation, &mut constellations) {
                    error!("Could not ensure constellation: {}", e);
                }

                let constellation = constellations.get_mut(&sentence.constellation).unwrap();

                if let Some(count) = sentence.satellites_in_view {
                    // Max satellite in view count.
                    match constellation.maximum_satellites_in_view_count {
                        Some(c) => {
                            if count > c {
                                // Count is larger than previous max.
                                constellation.maximum_satellites_in_view_count = Some(count)
                            }
                        },
                        None => {
                            // First recorded maximum count.
                            constellation.maximum_satellites_in_view_count = Some(count)
                        }
                    };

                    // Min satellite in view count.
                    match constellation.minimum_satellites_in_view_count {
                        Some(c) => {
                            if count < c {
                                // Count is less than previous max.
                                constellation.minimum_satellites_in_view_count = Some(count)
                            }
                        },
                        None => {
                            // First recorded maximum count.
                            constellation.minimum_satellites_in_view_count = Some(count)
                        }
                    };
                }

                for satellite in sentence.satellites {
                    match constellation.satellites_in_view.get_mut(&satellite.prn) {
                        Some(existing) => {
                            existing.elevation_degrees = satellite.elevation_degrees;
                            existing.azimuth_degrees = satellite.azimuth_degrees;
                            existing.snr = satellite.snr;
                        },
                        None => {
                            // First time we see this satellite.
                            constellation.satellites_in_view.insert(satellite.prn, SatelliteInfo {
                                prn: satellite.prn,
                                elevation_degrees: satellite.elevation_degrees,
                                azimuth_degrees: satellite.azimuth_degrees,
                                snr: satellite.snr,
                            });
                        }
                    }
                }
            },
            Err(e) => error!("Could not acquire constellations lock: {}", e)
        }
    }

    fn ensure_constellation(constellation: &GNSSConstellation,
                            constellations: &mut MutexGuard<HashMap<GNSSConstellation, GNSSConstellationData>>)
        -> Result<(), Error> {
        if constellations.get(constellation).is_none() {
            constellations.insert(constellation.clone(), Default::default());
        }

        Ok(())
    }

    pub fn calculate_metrics(&self) {
        // No metrics.
    }

    pub fn process_report(&self) {
        match self.constellations.lock() {
            Ok(constellations) => {
                let mut timer = Timer::new();

                // Generate JSON.
                let report = match serde_json::
                        to_string(&gnss_constellations_report::generate(&constellations, Utc::now())) {
                    Ok(report) => report,
                    Err(e) => {
                        error!("Could not serialize GNSS constellations report: {}", e);
                        return;
                    }
                };
                timer.stop();

                record_timer(
                    timer.elapsed_microseconds(),
                    "tables.gnss_monitor.timer.report_generation",
                    &self.metrics
                );

                // Send report.
                match self.leaderlink.lock() {
                    Ok(link) => {
                        if let Err(e) = link.send_report("gnss/constellations", report) {
                            error!("Could not submit GNSS constellations report: {}", e);
                        }
                    },
                    Err(e) => error!("Could not acquire leader link lock for GNSS constellations \
                        report submission: {}", e)
                }
            },
            Err(e) => error!("Could not acquire GNSS constellations lock: {}", e)
        }

        clear_mutex_hashmap(&self.constellations);
    }

}