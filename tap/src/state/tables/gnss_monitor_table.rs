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
use crate::wireless::positioning::axia::ubx::{UbxMonRfMessage, UbxRxmMeasxMessage};
use crate::wireless::positioning::gnss_constellation::GNSSConstellation;
use crate::wireless::positioning::gnss_constellation::GNSSConstellation::{GLONASS, GPS};
use crate::wireless::positioning::gnss_tools::{ubx_to_nmea_prn};
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
    pub fix_satellites: HashSet<u16>,
    pub maximum_altitude_meters: Option<f32>,
    pub minimum_altitude_meters: Option<f32>,
    pub maximum_pdop: Option<f32>,
    pub minimum_pdop: Option<f32>,
    pub satellites_in_view: HashMap<u8, SatelliteInfo>,
    pub minimum_satellites_in_view_count: Option<u8>,
    pub maximum_satellites_in_view_count: Option<u8>,
    pub maximum_jamming_indicator: Option<u8>,
    pub maximum_noise: Option<u16>,
    pub maximum_agc_count: Option<u16>,
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
    pub sno_readings: Vec<u8>,
    pub doppler_readings: Vec<i32>,
    pub multipath_indicator_max: Option<u8>,
    pub pseurange_rms_err_readings: Vec<u8>,
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

                let constellation = match constellations.get_mut(&sentence.constellation) {
                    Some(constellation) => constellation,
                    None => {
                        error!("Could not get GNSS constellation {}", sentence.constellation);
                        return;
                    }
                };

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

                let constellation = match constellations.get_mut(&sentence.constellation) {
                    Some(constellation) => constellation,
                    None => {
                        error!("Could not get GNSS constellation {}", sentence.constellation);
                        return;
                    }
                };

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

                let constellation = match constellations.get_mut(&sentence.constellation) {
                    Some(constellation) => constellation,
                    None => {
                        error!("Could not get GNSS constellation {}", sentence.constellation);
                        return;
                    }
                };

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
                    if satellite.snr.is_none() || satellite.snr == Some(0) {
                        /*
                         * Satellites reported with no SNR are not actively seen, but only expected
                         * based on GNSS almanac. We skip those for now, but may collect them later
                         * for more advanced detection techniques.
                         */
                        continue;
                    }

                    match constellation.satellites_in_view.get_mut(&satellite.prn) {
                        Some(existing) => {
                            existing.elevation_degrees = satellite.elevation_degrees;
                            existing.azimuth_degrees = satellite.azimuth_degrees;
                        },
                        None => {
                            // First time we see this satellite.
                            constellation.satellites_in_view.insert(satellite.prn, SatelliteInfo {
                                prn: satellite.prn,
                                elevation_degrees: satellite.elevation_degrees,
                                azimuth_degrees: satellite.azimuth_degrees,
                                sno_readings: vec![],
                                doppler_readings: vec![],
                                multipath_indicator_max: None,
                                pseurange_rms_err_readings: vec![],
                            });
                        }
                    }
                }
            },
            Err(e) => error!("Could not acquire constellations lock: {}", e)
        }
    }

    pub fn register_ubx_monrf_message(&mut self, message: Arc<UbxMonRfMessage>) {
        match self.constellations.lock() {
            Ok(mut constellations) => {
                if let Err(e) = Self::ensure_constellation(&message.constellation, &mut constellations) {
                    error!("Could not ensure constellation: {}", e);
                }

                let constellation = match constellations.get_mut(&message.constellation) {
                    Some(constellation) => constellation,
                    None => {
                        error!("Could not get GNSS constellation {}", message.constellation);
                        return;
                    }
                };

                let monrf_block = match message.blocks.len() {
                    1 => message.blocks.get(0).unwrap(),
                    _ => {
                        error!("Unexpected number of blocks in UBX MON-RF message: [{}]",
                            message.blocks.len());
                        return;
                    }
                };

                match constellation.maximum_jamming_indicator {
                    Some(x) => {
                        if monrf_block.jam_ind > x {
                            constellation.maximum_jamming_indicator = Some(x);
                        }
                    },
                    None => constellation.maximum_jamming_indicator = Some(monrf_block.jam_ind)
                }

                match constellation.maximum_noise {
                    Some(x) => {
                        if monrf_block.noise_per_ms > x {
                            constellation.maximum_noise = Some(x);
                        }
                    },
                    None => constellation.maximum_noise = Some(monrf_block.noise_per_ms)
                }

                match constellation.maximum_agc_count {
                    Some(x) => {
                        if monrf_block.agc_cnt > x {
                            constellation.maximum_agc_count = Some(x);
                        }
                    },
                    None => constellation.maximum_agc_count = Some(monrf_block.agc_cnt)
                }
            },
            Err(e) => error!("Could not acquire constellations lock: {}", e)
        }
    }

    pub fn register_ubx_rxm_measx_message(&mut self, message: Arc<UbxRxmMeasxMessage>) {
        match self.constellations.lock() {
            Ok(mut constellations) => {
                if let Err(e) = Self::ensure_constellation(&message.constellation, &mut constellations) {
                    error!("Could not ensure constellation: {}", e);
                }

                let constellation = match constellations.get_mut(&message.constellation) {
                    Some(constellation) => constellation,
                    None => {
                        error!("Could not get GNSS constellation {}", message.constellation);
                        return;
                    }
                };

                for satellite in &message.sats {
                    if satellite.sno == 0 {
                        continue;
                    }

                    let prn = match ubx_to_nmea_prn(&message.constellation, satellite.sv_id) {
                        Some(p) => p,
                        None => continue,
                    };

                    match constellation.satellites_in_view.get_mut(&prn) {
                        Some(existing) => {
                            // Existing satellite.
                            existing.doppler_readings.push(satellite.doppler_hz);
                            existing.pseurange_rms_err_readings.push(satellite.pseurange_rms_err);
                            existing.sno_readings.push(satellite.sno);

                            match existing.multipath_indicator_max {
                                Some(mpi) => {
                                    if satellite.mpath_indic > mpi {
                                        existing.multipath_indicator_max = Some(satellite.mpath_indic);
                                    }
                                },
                                None => {
                                    existing.multipath_indicator_max = Some(satellite.mpath_indic);
                                }
                            }

                        },
                        None => {
                            // First time we see this satellite.
                            constellation.satellites_in_view.insert(prn, SatelliteInfo {
                                prn,
                                elevation_degrees: None,
                                azimuth_degrees: None,
                                sno_readings: vec![satellite.sno],
                                doppler_readings: vec![satellite.doppler_hz],
                                multipath_indicator_max: Some(satellite.mpath_indic),
                                pseurange_rms_err_readings: vec![satellite.pseurange_rms_err],
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