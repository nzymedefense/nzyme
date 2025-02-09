use std::collections::{HashMap, HashSet};
use std::sync::{Arc, Mutex};
use chrono::{DateTime, Utc};
use log::{error, info};
use uuid::Uuid;
use crate::metrics::Metrics;
use crate::protocols::detection::taggers::remoteid::messages::{LocationVectorMessage, OperationalStatus, UavRemoteIdMessage, UavType};
use crate::state::tables::uav_table::DetectionSource::RemoteId;

pub struct UavTable {
    uavs: Mutex<HashMap<String, Uav>>,
    metrics: Arc<Mutex<Metrics>>
}

#[derive(Debug)]
enum DetectionSource {
    RemoteId
}

#[derive(Debug)]
struct Uav {
    uuid: Uuid,
    detection_source: DetectionSource,
    last_seen: DateTime<Utc>,
    uav_type: Option<String>,
    uav_ids: HashSet<UavId>,
    operator_ids: HashSet<String>,
    flight_descriptions: HashSet<String>,
    vector_reports: Vec<VectorReport>,
    operator_location_reports: Vec<OperatorLocationReport>,
}

#[derive(Debug, Eq, PartialEq, Hash)]
struct UavId {
    id_type: String,
    id: String
}

#[derive(Debug)]
struct VectorReport {
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

#[derive(Debug)]
struct OperatorLocationReport {
    timestamp: DateTime<Utc>,
    location_types: HashSet<String>,
    latitude: Option<f64>,
    longitude: Option<f64>,
    altitude: Option<f32>
}

impl UavTable {

    pub fn new(metrics: Arc<Mutex<Metrics>>) -> Self {
        UavTable {
            uavs: Mutex::new(HashMap::new()),
            metrics
        }
    }

    pub fn register_remote_id_message(&self, message: Arc<UavRemoteIdMessage>) {
        match self.uavs.lock() {
            Ok(mut uavs) => {
                match uavs.get_mut(&message.bssid) {
                    Some(uav) => {
                        uav.last_seen = message.timestamp;
                    },
                    None => {
                        let mut uav_ids = HashSet::new();
                        for id in &message.ids {
                            uav_ids.insert(
                                UavId { id_type: id.id_type.to_string(), id: id.id.clone() }
                            );
                        }

                        let mut operator_ids = HashSet::new();
                        if let Some(operator_id) = &message.operator_license_id {
                            operator_ids.insert(operator_id.operator_id.clone());
                        }

                        let mut flight_descriptions = HashSet::new();
                        if let Some(flight_description) = &message.self_id {
                            flight_descriptions.insert(flight_description.flight_description.clone());
                        }

                        let mut vector_reports = Vec::new();
                        if let Some(vector) = &message.location_and_vector {
                            vector_reports.push(VectorReport {
                                timestamp: message.timestamp,
                                operational_status: Some(vector.operational_status.to_string()),
                                height_type: Some(vector.height_type.to_string()),
                                ground_track: vector.ground_track.clone(),
                                speed: Some(vector.speed),
                                vertical_speed: Some(vector.vertical_speed),
                                latitude: Some(vector.latitude),
                                longitude: Some(vector.longitude),
                                altitude_pressure: vector.altitude_pressure.clone(),
                                altitude_geodetic: vector.altitude_geodetic.clone(),
                                height: vector.height.clone(),
                                horizontal_accuracy: Some(vector.horizontal_accuracy),
                                vertical_accuracy: Some(vector.vertical_accuracy),
                                barometer_accuracy: Some(vector.barometer_accuracy),
                                speed_accuracy: Some(vector.speed_accuracy)
                            })
                        }

                        let mut operator_location_reports = Vec::new();
                        if let Some(location) = &message.system {
                            let mut location_types = HashSet::new();
                            location_types.insert(location.operator_location_type.to_string());

                            operator_location_reports.push(OperatorLocationReport {
                                timestamp: message.timestamp,
                                location_types,
                                latitude: Some(location.operator_location_latitude),
                                longitude: Some(location.operator_location_longitude),
                                altitude: location.operator_altitude.clone()
                            });
                        }

                        uavs.insert(message.bssid.clone(), Uav {
                            uuid: Uuid::new_v4(),
                            detection_source: RemoteId,
                            last_seen: message.timestamp,
                            uav_type: message.uav_type.as_ref().map(|t| t.to_string()),
                            uav_ids,
                            operator_ids,
                            flight_descriptions,
                            vector_reports,
                            operator_location_reports
                        });
                    }
                }
            },
            Err(e) => {
                error!("Could not acquire UAV table map mutex: {}", e);
            }
        }

        info!("UAVs: {:?}", self.uavs);
    }

}