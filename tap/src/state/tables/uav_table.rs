use sha2::Digest;
use std::collections::{HashMap, HashSet};
use std::sync::{Arc, Mutex};
use chrono::{DateTime, Utc};
use libc::__u32;
use log::{error, info};
use sha2::Sha256;
use strum_macros::Display;
use crate::helpers::timer::{record_timer, Timer};
use crate::link::leaderlink::Leaderlink;
use crate::link::reports::uavs_report;
use crate::metrics::Metrics;
use crate::protocols::detection::taggers::remoteid::messages::{RemoteIdType, UavRemoteIdMessage, UavType};
use crate::state::tables::table_helpers::clear_mutex_hashmap;
use crate::state::tables::uav_table::DetectionSource::{RemoteIdBluetooth, RemoteIdWiFi};
use crate::wireless::dot11::engagement::engagement_control::EngagementControl;
use crate::wireless::dot11::engagement::uav_engagement_request::UavEngagementRequest;
use crate::wireless::dot11::supported_frequency::SupportedChannelWidth;

pub struct UavTable {
    uavs: Mutex<HashMap<String, Uav>>,
    metrics: Arc<Mutex<Metrics>>,
    leaderlink: Arc<Mutex<Leaderlink>>,
    engagement_control: Arc<EngagementControl>
}

#[derive(Debug, Display)]
pub enum DetectionSource {
    RemoteIdWiFi,
    RemoteIdBluetooth
}

#[derive(Debug)]
pub struct Uav {
    pub identifier: String,
    pub rssis: Vec<i8>,
    pub detection_source: DetectionSource,
    pub first_seen: DateTime<Utc>,
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
    pub location_type: String,
    pub latitude: f64,
    pub longitude: f64,
    pub altitude: Option<f32>
}

impl UavTable {

    pub fn new(leaderlink: Arc<Mutex<Leaderlink>>,
               metrics: Arc<Mutex<Metrics>>,
               engagement_control: Arc<EngagementControl>) -> Self {
        UavTable {
            uavs: Mutex::new(HashMap::new()),
            leaderlink,
            metrics,
            engagement_control
        }
    }

    pub fn register_remote_id_message(&self, message: Arc<UavRemoteIdMessage>) {
        let identifier = Self::build_remote_id_identifier(message.as_ref());

        match self.uavs.lock() {
            Ok(mut uavs) => {
                match uavs.get_mut(&identifier) {
                    Some(uav) => {
                        // Existing UAV.
                        uav.last_seen = message.timestamp;
                        
                        if let Some(uav_type) = &message.uav_type {
                            uav.uav_type = Some(uav_type.to_string());
                        }

                        uav.rssis.extend(message.rssis.clone());

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

                        let detection_source = match message.remote_id_type {
                            RemoteIdType::WiFi => RemoteIdWiFi,
                            RemoteIdType::Bluetooth => RemoteIdBluetooth
                        };

                        uavs.insert(identifier.clone(), Uav {
                            identifier: identifier.clone(),
                            rssis: message.rssis.clone(),
                            detection_source,
                            first_seen: message.timestamp,
                            last_seen: message.timestamp,
                            uav_type: message.uav_type.as_ref().map(|t| t.to_string()),
                            uav_ids,
                            operator_ids,
                            flight_descriptions,
                            vector_reports,
                            operator_location_reports
                        });
                        
                        let tracking_request = UavEngagementRequest {
                            uav_id: identifier.clone(),
                            mac_address: message.bssid.clone(),
                            initial_frequency: message.frequency,
                            initial_channel_width: SupportedChannelWidth::Mhz20
                        };

                        // Request tracking.
                        if let Err(e) = self.engagement_control.engage_uav(tracking_request) {
                            error!("Tracking request for UAV [{}] failed: {}", identifier, e);
                        }
                    }
                }
            },
            Err(e) => {
                error!("Could not acquire UAV table map mutex: {}", e);
            }
        }
    }

    pub fn process_report(&self) {
        match self.uavs.lock() {
            Ok(uavs) => {
                // Generate JSON.
                let mut timer = Timer::new();
                let report = match serde_json::to_string(&uavs_report::generate(&uavs)) {
                    Ok(report) => report,
                    Err(e) => {
                        error!("Could not serialize UAVs report: {}", e);
                        return;
                    }
                };
                timer.stop();
                record_timer(
                    timer.elapsed_microseconds(),
                    "tables.uav.timer.report_generation",
                    &self.metrics
                );

                // Send report.
                match self.leaderlink.lock() {
                    Ok(link) => {
                        if let Err(e) = link.send_report("uav/uavs", report) {
                            error!("Could not submit UAVs report: {}", e);
                        }
                    },
                    Err(e) => error!("Could not acquire leader link lock for UAVs \
                                        report submission: {}", e)
                }

            },
            Err(e) => {
                error!("Could not acquire UAV table map mutex: {}", e);
            }
        }

        clear_mutex_hashmap(&self.uavs);
    }

    /*
     * Builds a stable identifier using multiple attributes of a Remote ID message to identify
     * a drone between table cycles.
     */
    fn build_remote_id_identifier(message: &UavRemoteIdMessage) -> String {
        let mut identifier = message.bssid.clone();

        if let Some(uav_type) = &message.uav_type {
            identifier.push_str(&uav_type.to_string());
        }

        if let Some(operator_id) = &message.operator_license_id {
            identifier.push_str(&operator_id.operator_id);
        }

        let mut sorted_ids = message.ids.clone();
        sorted_ids.sort_by(|a, b| a.id.cmp(&b.id));
        for id in &message.ids {
            identifier.push_str(&id.id);
        }

        let mut hasher = Sha256::new();
        hasher.update(identifier.as_bytes());
        format!("{:x}", hasher.finalize())
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
            operator_location_reports.push(OperatorLocationReport {
                timestamp: message.timestamp,
                location_type: location.operator_location_type.to_string(),
                latitude: location.operator_location_latitude,
                longitude: location.operator_location_longitude,
                altitude: location.operator_altitude
            });
        }
    }

}