use std::collections::{HashMap, HashSet};
use std::sync::MutexGuard;
use chrono::{DateTime, Utc};
use serde::Serialize;
use crate::state::tables::uav_table::Uav;

#[derive(Serialize)]
pub struct UavsReport {
    uavs: Vec<UavReport>
}

#[derive(Serialize)]
pub struct UavReport {
    identifier: String,
    rssis: Vec<i8>,
    detection_source: String,
    first_seen: DateTime<Utc>,
    last_seen: DateTime<Utc>,
    uav_type: Option<String>,
    uav_ids: HashSet<UavIdReport>,
    operator_ids: HashSet<String>,
    flight_descriptions: HashSet<String>,
    vector_reports: Vec<VectorReport>,
    operator_location_reports: Vec<OperatorLocationReport>
}

#[derive(Serialize, Eq, PartialEq, Hash)]
pub struct UavIdReport {
    id_type: String,
    id: String
}

#[derive(Serialize)]
pub struct VectorReport {
    timestamp: DateTime<Utc>,
    operational_status: Option<String>,
    height_type: Option<String>,
    ground_track: Option<u16>,
    speed: Option<f32>,
    vertical_speed: Option<f32>,
    latitude: Option<f64>,
    longitude: Option<f64>,
    altitude_pressure: Option<f32>,
    altitude_geodetic: Option<f32>,
    height: Option<f32>,
    horizontal_accuracy: Option<u8>,
    vertical_accuracy: Option<u8>,
    barometer_accuracy: Option<u8>,
    speed_accuracy: Option<u8>
}

#[derive(Serialize)]
pub struct OperatorLocationReport {
    timestamp: DateTime<Utc>,
    location_type: String,
    latitude: f64,
    longitude: f64,
    altitude: Option<f32>
}

pub fn generate(x: &MutexGuard<HashMap<String, Uav>>) -> UavsReport {
    let mut uavs: Vec<UavReport> = Vec::new();

    for uav in x.values() {
        let uav_ids = uav.uav_ids.iter()
            .map(|id| UavIdReport { id: id.id.clone(), id_type: id.id_type.clone() })
            .collect();

        let mut vector_reports = Vec::new();
        for vector in &uav.vector_reports {
            vector_reports.push(VectorReport {
                timestamp: vector.timestamp,
                operational_status: vector.operational_status.clone(),
                height_type: vector.height_type.clone(),
                ground_track: vector.ground_track,
                speed: vector.speed,
                vertical_speed: vector.vertical_speed,
                latitude: vector.latitude,
                longitude: vector.longitude,
                altitude_pressure: vector.altitude_pressure,
                altitude_geodetic: vector.altitude_geodetic,
                height: vector.height,
                horizontal_accuracy: vector.horizontal_accuracy,
                vertical_accuracy: vector.vertical_accuracy,
                barometer_accuracy: vector.barometer_accuracy,
                speed_accuracy: vector.speed_accuracy
            });
        }

        let mut operator_location_reports = Vec::new();
        for operator_location in &uav.operator_location_reports {
            operator_location_reports.push(OperatorLocationReport {
                timestamp: operator_location.timestamp,
                location_type: operator_location.location_type.clone(),
                latitude: operator_location.latitude,
                longitude: operator_location.longitude,
                altitude: operator_location.altitude
            });
        }

        uavs.push(UavReport {
            identifier: uav.identifier.clone(),
            rssis: uav.rssis.clone(),
            detection_source: uav.detection_source.to_string(),
            first_seen: uav.first_seen,
            last_seen: uav.last_seen,
            uav_type: uav.uav_type.clone(),
            uav_ids,
            operator_ids: uav.operator_ids.clone(),
            flight_descriptions: uav.flight_descriptions.clone(),
            vector_reports,
            operator_location_reports
        })
    }

    UavsReport { uavs }
}
