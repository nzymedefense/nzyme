use std::collections::HashMap;

use serde::{Serialize, ser::SerializeStruct};
use chrono::{Utc, DateTime};
use crate::alerting::alert_types::{AlertAttribute, Dot11AlertType};

#[derive(Serialize)]
pub struct StatusReport {
    pub version: String,
    pub timestamp: DateTime<Utc>,
    pub processed_bytes: TotalWithAverage,
    pub buses: Vec<BusReport>,
    pub captures: Vec<CaptureReport>,
    pub system_metrics: SystemMetricsReport,
    pub gauges_long: HashMap<String, i128>,
    pub timers: HashMap<String, TimerReport>,
    pub log_counts: HashMap<String, u128>,
    pub rpi: Option<String>
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
    pub memory_free: u64,
    pub rpi_temperature: Option<f32>
}

#[derive(Serialize)]
pub struct TimerReport {
    pub mean: f64,
    pub p99: f64
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
    pub dot11: Dot11TableReport
}

#[derive(Serialize)]
pub struct DnsTableReport {
    pub ips: HashMap<String, DnsIpStatisticsReport>,
    pub entropy_log: Vec<DNSEntropyLog>,
    pub queries: Vec<DNSLogReport>,
    pub responses: Vec<DNSLogReport>
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
pub struct DNSEntropyLog {
    pub transaction_id: u16,
    pub entropy: f32,
    pub zscore: f32,
    pub entropy_mean: f32,
    pub timestamp: DateTime<Utc>
}

#[derive(Serialize)]
pub struct DNSLogReport {
    pub transaction_id: Option<u16>,
    pub client_address: String,
    pub server_address: String,
    pub client_mac: Option<String>,
    pub server_mac: Option<String>,
    pub client_port: u16,
    pub server_port: u16,
    pub data_value: String,
    pub data_value_etld: Option<String>,
    pub data_type: String,
    pub timestamp: DateTime<Utc>
}

#[derive(Serialize)]
pub struct Dot11TableReport {
    pub bssids: HashMap<String, BssidReport>,
    pub clients: HashMap<String, Dot11ClientReport>,
    pub disco: Dot11DiscoReport,
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
    pub wildcard_probe_requests: u128,
    pub signal_strength: SignalStrengthReport
}

#[derive(Serialize)]
pub struct Dot11AlertReport {
    pub alert_type: Dot11AlertType,
    pub signal_strength: i8,
    pub attributes: HashMap<String, AlertAttribute>
}

#[derive(Serialize)]
pub struct Dot11DiscoReport {
    pub deauth: HashMap<String, DiscoTransmitterReport>,
    pub disassoc: HashMap<String, DiscoTransmitterReport>
}

#[derive(Serialize)]
pub struct DiscoTransmitterReport {
    pub bssid: String,
    pub sent_frames: u128,
    pub receivers: HashMap<String, u128>
}

#[derive(Serialize)]
pub struct AdvertisedNetworkReport {
    pub security: Vec<SecurityInformationReport>,
    pub fingerprints: Vec<String>,
    pub beacon_advertisements: u128,
    pub proberesp_advertisements: u128,
    pub rates: Vec<f32>,
    pub wps: Vec<bool>,
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
    pub suites: Dot11CipherSuites,
    pub pmf: PmfReport
}

#[derive(Serialize)]
pub struct Dot11CipherSuites {
    pub group_cipher: String,
    pub pairwise_ciphers: Vec<String>,
    pub key_management_modes: Vec<String>
}

#[derive(Serialize)]
pub enum PmfReport {
    Required,
    Optional,
    Disabled,
    Unavailable
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
    pub rx_bytes: u128,
    pub signal_strength: SignalStrengthReport
}

#[derive(Serialize)]
pub struct NodeHelloReport {
    pub wifi_device_assignments: HashMap<String, Vec<WiFiSupportedFrequencyReport>>,
    pub wifi_device_cycle_times: HashMap<String, u64>
}

#[derive(Serialize)]
pub struct WiFiSupportedFrequencyReport {
    pub frequency: u32,
    pub channel_widths: Vec<String>
}

impl TotalWithAverage {

    pub fn from_metric(m: &crate::metrics::TotalWithAverage) -> Self {
        TotalWithAverage {
            total: m.total,
            average: m.avg
        }
    }

}

impl Serialize for TablesReport {

    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where S: serde::Serializer {
        let mut state = serializer.serialize_struct("TablesReport", 3)?;
        state.serialize_field("timestamp", &self.timestamp.to_rfc3339())?;
        state.serialize_field("arp", &self.arp)?;
        state.serialize_field("dns", &self.dns)?;
        state.serialize_field("remoteid", &self.dot11)?;
        state.end()
    }

}
