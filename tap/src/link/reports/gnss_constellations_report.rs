use std::collections::{HashMap, HashSet};
use std::sync::MutexGuard;
use chrono::{DateTime, Utc};
use serde::Serialize;
use crate::state::tables::gnss_monitor_table::GNSSConstellationData;
use crate::wireless::positioning::gnss::gnss_constellation::GNSSConstellation;

#[derive(Serialize)]
pub struct GNSSConstellationsReport {
    constellations: HashMap<String, GNSSConstellationReport>
}

#[derive(Serialize)]
pub struct GNSSConstellationReport {
    fixes: HashSet<String>,
    maximum_time_deviation_ms: Option<i64>,
    positions: Vec<LatLonReport>,
    maximum_fix_satellite_count: Option<u8>,
    minimum_fix_satellite_count: Option<u8>,
    fix_satellites: HashSet<u16>,
    maximum_altitude_meters: Option<f32>,
    minimum_altitude_meters: Option<f32>,
    maximum_pdop: Option<f32>,
    minimum_pdop: Option<f32>,
    satellites_in_view: Vec<SatelliteInfoReport>,
    minimum_satellites_in_view_count: Option<u8>,
    maximum_satellites_in_view_count: Option<u8>,
    timestamp: DateTime<Utc>
}

#[derive(Serialize)]
pub struct LatLonReport {
    pub lat: f64,
    pub lon: f64
}

#[derive(Serialize)]
pub struct SatelliteInfoReport {
    pub prn: u8,
    pub elevation_degrees: Option<u8>,
    pub azimuth_degrees: Option<u16>,
    pub snr: Option<u8>,
}

pub fn generate(constellations: &MutexGuard<HashMap<GNSSConstellation, GNSSConstellationData>>,
                timestamp: DateTime<Utc>) -> GNSSConstellationsReport {
    let mut constellations_report: HashMap<String, GNSSConstellationReport> = HashMap::new();

    for (name, data) in constellations.iter() {
        let constellation = GNSSConstellationReport {
            fixes: data.fixes.iter().map(|f| f.to_string()).collect(),
            maximum_time_deviation_ms: data.maximum_time_deviation_ms,
            positions: data.positions.iter()
                .map(|p| LatLonReport { lat: p.lat, lon: p.lon } ).collect(),
            maximum_fix_satellite_count: data.maximum_fix_satellite_count,
            minimum_fix_satellite_count: data.minimum_fix_satellite_count,
            fix_satellites: data.fix_satellites.clone(),
            maximum_altitude_meters: data.maximum_altitude_meters,
            minimum_altitude_meters: data.minimum_altitude_meters,
            maximum_pdop: data.maximum_pdop,
            minimum_pdop: data.minimum_pdop,
            satellites_in_view: data.satellites_in_view.values()
                .map(|s| SatelliteInfoReport{
                    prn: s.prn,
                    elevation_degrees: s.elevation_degrees,
                    azimuth_degrees: s.azimuth_degrees,
                    snr: s.snr
                } ).collect(),
            minimum_satellites_in_view_count: data.minimum_satellites_in_view_count,
            maximum_satellites_in_view_count: data.maximum_satellites_in_view_count,
            timestamp
        };

        constellations_report.insert(name.to_string(), constellation);
    }

    GNSSConstellationsReport { constellations: constellations_report }
}