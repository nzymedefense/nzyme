use std::collections::HashMap;

use serde::{Serialize, ser::SerializeStruct};
use chrono::{Utc, DateTime};
use crate::alerting::alert_types::{Dot11AlertAttribute, Dot11AlertType};

pub struct StatusReport {
    pub version: String,
    pub timestamp: DateTime<Utc>,
    pub processed_bytes: TotalWithAverage,
    pub bus: BusReport,
    pub captures: Vec<CaptureReport>,
    pub system_metrics: SystemMetricsReport,
    pub gauges_long: HashMap<String, i128>
}

#[derive(Serialize)]
pub struct BusReport {
    pub name: String,
    pub channels: Vec<ChannelReport>
}

#[derive(Serialize)]
pub struct ChannelReport {
    pub name: String,
    pub capacity: u128,
    pub watermark: u128,
    pub errors: TotalWithAverage,
    pub throughput_bytes: TotalWithAverage,
    pub throughput_messages: TotalWithAverage,
}

#[derive(Serialize)]
pub struct TotalWithAverage {
    pub total: u128,
    pub average: u128
}

#[derive(Serialize)]
pub struct SystemMetricsReport {
    pub cpu_load: f32,
    pub memory_total: u64,
    pub memory_free: u64
}

#[derive(Serialize)]
pub struct CaptureReport {
    pub capture_type: String,
    pub interface_name: String,
    pub is_running: bool,
    pub received: u32,
    pub dropped_buffer: u32,
    pub dropped_interface: u32
}

pub struct TablesReport {
    pub timestamp: DateTime<Utc>,
    pub arp: HashMap<String, HashMap<String, u128>>,
    pub dns: DnsTableReport,
    pub l4: L4TableReport,
    pub dot11: Dot11TableReport
}

#[derive(Serialize)]
pub struct DnsTableReport {
    pub ips: HashMap<String, DnsIpStatisticsReport>,
    pub nxdomains: Vec<NXDomainLogReport>,
    pub entropy_log: Vec<DNSEntropyLog>,
    pub pairs: HashMap<String, HashMap<String, u128>>,
    pub retro_queries: Vec<DNSRetroQueryLogReport>,
    pub retro_responses: Vec<DNSRetroResponseLogReport>
}

#[derive(Serialize)]
pub struct DnsIpStatisticsReport {
    pub request_count: u128,
    pub request_bytes: u128,
    pub response_count: u128,
    pub response_bytes: u128,
    pub nxdomain_count: u128
}

#[derive(Serialize)]
pub struct NXDomainLogReport {
    pub ip: String,
    pub server: String,
    pub query_value: String,
    pub data_type: String,
    pub timestamp: DateTime<Utc>
}

#[derive(Serialize)]
pub struct DNSEntropyLog {
    pub log_type: String,
    pub entropy: f32,
    pub zscore: f32,
    pub value: String,
    pub timestamp: DateTime<Utc>
}

#[derive(Serialize)]
pub struct DNSRetroQueryLogReport {
    pub ip: String,
    pub server: String,
    pub source_mac: String,
    pub destination_mac: String,
    pub port: u16,
    pub query_value: String,
    pub data_type: String,
    pub timestamp: DateTime<Utc>
}

#[derive(Serialize)]
pub struct DNSRetroResponseLogReport {
    pub ip: String,
    pub server: String,
    pub source_mac: String,
    pub destination_mac: String,
    pub response_value: String,
    pub data_type: String,
    pub timestamp: DateTime<Utc>
}

#[derive(Serialize)]
pub struct L4TableReport {
    pub retro_pairs: Vec<L4RetroPairReport>
}

#[derive(Serialize, Eq, PartialEq, Hash)]
pub struct L4RetroPairReport {
    pub l4_type: String,
    pub source_mac: String,
    pub destination_mac: String,
    pub source_address: String,
    pub destination_address: String,
    pub source_port: u16,
    pub destination_port: u16,
    pub connection_count: u64,
    pub size: u64,
    pub timestamp: DateTime<Utc>
}

#[derive(Serialize)]
pub struct Dot11TableReport {
    pub bssids: HashMap<String, BssidReport>,
    pub clients: HashMap<String, Dot11ClientReport>,
    pub alerts: Vec<Dot11AlertReport>
}

#[derive(Serialize)]
pub struct BssidReport {
    pub advertised_networks: HashMap<String, AdvertisedNetworkReport>,
    pub clients: HashMap<String, Dot11ClientStatisticsReport>,
    pub hidden_ssid_frames: u128,
    pub signal_strength: SignalStrengthReport,
    pub fingerprints: Vec<String>,
}

#[derive(Serialize)]
pub struct Dot11ClientReport {
    pub probe_request_ssids: HashMap<String, u128>,
    pub wildcard_probe_requests: u128
}

#[derive(Serialize)]
pub struct Dot11AlertReport {
    pub alert_type: Dot11AlertType,
    pub signal_strength: i8,
    pub attributes: HashMap<String, Dot11AlertAttribute>
}

#[derive(Serialize)]
pub struct AdvertisedNetworkReport {
    pub security: Vec<SecurityInformationReport>,
    pub fingerprints: Vec<String>,
    pub beacon_advertisements: u128,
    pub proberesp_advertisements: u128,
    pub rates: Vec<f32>,
    pub wps: bool,
    pub signal_strength: SignalStrengthReport,
    pub signal_histogram: HashMap<u16, HashMap<i8, u128>>,
    pub infrastructure_types: Vec<String>,
    pub channel_statistics: HashMap<u16, HashMap<String, Dot11ChannelStatisticsReport>>
}

#[derive(Serialize)]
pub struct SignalStrengthReport {
    pub min: i8,
    pub max: i8,
    pub average: f32
}

#[derive(Serialize)]
pub struct SecurityInformationReport {
    pub protocols: Vec<String>,
    pub suites: Dot11CipherSuites
}

#[derive(Serialize)]
pub struct Dot11CipherSuites {
    pub group_cipher: String,
    pub pairwise_ciphers: Vec<String>,
    pub key_management_modes: Vec<String>
}

#[derive(Serialize)]
pub struct Dot11ChannelStatisticsReport {
    pub frames: u128,
    pub bytes: u128
}

#[derive(Serialize)]
pub struct Dot11ClientStatisticsReport {
    pub tx_frames: u128,
    pub tx_bytes: u128,
    pub rx_frames: u128,
    pub rx_bytes: u128
}

impl TotalWithAverage {

    pub fn from_metric(m: &crate::metrics::TotalWithAverage) -> Self {
        TotalWithAverage {
            total: m.total,
            average: m.avg
        }
    }

}

impl Serialize for StatusReport {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where S: serde::Serializer {
        let mut state = serializer.serialize_struct("StatusReport", 7)?;
        state.serialize_field("version", &self.version)?;
        state.serialize_field("timestamp", &self.timestamp.to_rfc3339())?;
        state.serialize_field("processed_bytes", &self.processed_bytes)?;
        state.serialize_field("bus", &self.bus)?;
        state.serialize_field("system_metrics", &self.system_metrics)?;
        state.serialize_field("captures", &self.captures)?;
        state.serialize_field("gauges_long", &self.gauges_long)?;
        state.end()
    }
}

impl Serialize for TablesReport {

    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where S: serde::Serializer {
        let mut state = serializer.serialize_struct("TablesReport", 3)?;
        state.serialize_field("timestamp", &self.timestamp.to_rfc3339())?;
        state.serialize_field("arp", &self.arp)?;
        state.serialize_field("dns", &self.dns)?;
        state.serialize_field("l4", &self.l4)?;
        state.serialize_field("dot11", &self.dot11)?;
        state.end()
    }

}
