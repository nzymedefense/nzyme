use std::collections::{HashMap, HashSet};
use std::sync::{Arc, Mutex};
use chrono::{DateTime, Utc};
use log::{error, info};
use strum_macros::Display;
use uuid::Uuid;
use crate::metrics::Metrics;
use crate::protocols::detection::taggers::remoteid::messages::{LocationVectorMessage, OperationalStatus, UavRemoteIdMessage, UavType};
use crate::state::tables::uav_table::DetectionSource::RemoteId;

pub struct UavTable {
    uavs: Mutex<HashMap<String, Uav>>,
    metrics: Arc<Mutex<Metrics>>
}

#[derive(Debug, Display)]
pub enum DetectionSource {
    RemoteId
}

#[derive(Debug)]
pub struct Uav {
    pub uuid: Uuid,
    pub detection_source: DetectionSource,
    pub last_seen: DateTime<Utc>,
    pub uav_type: Option<String>,
    pub uav_ids: HashSet<UavId>,
    pub operator_ids: HashSet<String>,
    pub flight_descriptions: HashSet<String>,
    pub vector_reports: Vec<VectorReport>,
    pub operator_location_reports: Vec<OperatorLocationReport>,
}

#[derive(Debug, Eq, PartialEq, Hash)]
pub struct UavId {
    pub id_type: String,
    pub id: String
}

#[derive(Debug)]
pub struct VectorReport {
    pub timestamp: DateTime<Utc>,
    pub operational_status: Option<String>,
    pub height_type: Option<String>,
    pub ground_track: Option<u16>,
    pub speed: Option<f32>,
    pub vertical_speed: Option<f32>,
    pub latitude: Option<f64>,
    pub longitude: Option<f64>,
    pub altitude_pressure: Option<f32>,
    pub altitude_geodetic: Option<f32>,
    pub height: Option<f32>,
    pub horizontal_accuracy: Option<u8>,
    pub vertical_accuracy: Option<u8>,
    pub barometer_accuracy: Option<u8>,
    pub speed_accuracy: Option<u8>
}

#[derive(Debug)]
pub struct OperatorLocationReport {
    pub timestamp: DateTime<Utc>,
    pub location_types: HashSet<String>,
    pub latitude: Option<f64>,
    pub longitude: Option<f64>,
    pub altitude: Option<f32>
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
                        // Existing UAV.
                        uav.last_seen = message.timestamp;
                        
                        if let Some(uav_type) = &message.uav_type {
                            uav.uav_type = Some(uav_type.to_string());
                        }

                        Self::update_uav_ids(message.as_ref(), &mut uav.uav_ids);
                        Self::update_operator_ids(message.as_ref(), &mut uav.operator_ids);
                        Self::update_flight_descriptions(
                            message.as_ref(),
                            &mut uav.flight_descriptions);
                        Self::update_vector_reports(message.as_ref(), &mut uav.vector_reports);
                        Self::update_operator_location_reports(
                            message.as_ref(),
                            &mut uav.operator_location_reports);
                    },
                    None => {
                        // New UAV.
                        let mut uav_ids = HashSet::new();
                        Self::update_uav_ids(message.as_ref(), &mut uav_ids);

                        let mut operator_ids = HashSet::new();
                        Self::update_operator_ids(message.as_ref(), &mut operator_ids);

                        let mut flight_descriptions = HashSet::new();
                        Self::update_flight_descriptions(
                            message.as_ref(),
                            &mut flight_descriptions
                        );

                        let mut vector_reports = Vec::new();
                        Self::update_vector_reports(message.as_ref(), &mut vector_reports);

                        let mut operator_location_reports = Vec::new();
                        Self::update_operator_location_reports(
                            message.as_ref(),
                            &mut operator_location_reports
                        );

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

    pub fn process_report(&self) {
        match self.uavs.lock() {
            Ok(mut uavs) => {
                // Clean up.
                uavs.clear();
            },
            Err(e) => {
                error!("Could not acquire UAV table map mutex: {}", e);
            }
        }
    }

    pub fn calculate_metrics(&self) {
        let (uavs_size, vector_reports): (i128, i128) = match self.uavs.lock() {
            Ok(uavs) => {
                let mut vector_reports: i128 = 0;
                uavs.values().for_each(|s| vector_reports += s.vector_reports.len() as i128);

                (uavs.len() as i128, vector_reports)
            },
            Err(e) => {
                error!("Could not acquire mutex to calculate UAV table sizes: {}", e);

                (-1, -1)
            }
        };

        match self.metrics.lock() {
            Ok(mut metrics) => {
                metrics.set_gauge("tables.uav.uavs.size", uavs_size);
                metrics.set_gauge("tables.uav.uavs.vector_reports", vector_reports);
            },
            Err(e) => error!("Could not acquire metrics mutex: {}", e)
        }
    }

    fn update_uav_ids(message: &UavRemoteIdMessage, uav_ids: &mut HashSet<UavId>) {
        for id in &message.ids {
            uav_ids.insert(
                UavId { id_type: id.id_type.to_string(), id: id.id.clone() }
            );
        }
    }

    fn update_operator_ids(message: &UavRemoteIdMessage, operator_ids: &mut HashSet<String>) {
        if let Some(operator_id) = &message.operator_license_id {
            operator_ids.insert(operator_id.operator_id.clone());
        }
    }

    fn update_flight_descriptions(message: &UavRemoteIdMessage, descriptions: &mut HashSet<String>) {
        if let Some(flight_description) = &message.self_id {
            descriptions.insert(flight_description.flight_description.clone());
        }
    }

    fn update_vector_reports(message: &UavRemoteIdMessage, vector_reports: &mut Vec<VectorReport>) {
        if let Some(vector) = &message.location_and_vector {
            vector_reports.push(VectorReport {
                timestamp: message.timestamp,
                operational_status: Some(vector.operational_status.to_string()),
                height_type: Some(vector.height_type.to_string()),
                ground_track: vector.ground_track,
                speed: Some(vector.speed),
                vertical_speed: Some(vector.vertical_speed),
                latitude: Some(vector.latitude),
                longitude: Some(vector.longitude),
                altitude_pressure: vector.altitude_pressure,
                altitude_geodetic: vector.altitude_geodetic,
                height: vector.height,
                horizontal_accuracy: Some(vector.horizontal_accuracy),
                vertical_accuracy: Some(vector.vertical_accuracy),
                barometer_accuracy: Some(vector.barometer_accuracy),
                speed_accuracy: Some(vector.speed_accuracy)
            })
        }
    }

    fn update_operator_location_reports(message: &UavRemoteIdMessage,
                                        operator_location_reports: &mut Vec<OperatorLocationReport>) {
        if let Some(location) = &message.system {
            let mut location_types = HashSet::new();
            location_types.insert(location.operator_location_type.to_string());

            operator_location_reports.push(OperatorLocationReport {
                timestamp: message.timestamp,
                location_types,
                latitude: Some(location.operator_location_latitude),
                longitude: Some(location.operator_location_longitude),
                altitude: location.operator_altitude
            });
        }
    }

}