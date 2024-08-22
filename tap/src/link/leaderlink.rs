use anyhow::bail;
use chrono::Utc;
use log::{error, info};
use reqwest::{blocking::Client, blocking::Response, header::{HeaderMap, AUTHORIZATION}, Error, Url};
use std::{thread::self, time::Duration, sync::{Mutex, Arc}, collections::HashMap};
use systemstat::{System, Platform};
use strum::IntoEnumIterator;

use crate::{
    configuration::Configuration,
    metrics::Metrics, messagebus::bus::Bus
};
use crate::dot11::supported_frequency::{SupportedChannelWidth, SupportedFrequency};
use crate::link::payloads::{NodeHelloReport, TimerReport, WiFiSupportedFrequencyReport};
use crate::messagebus::channel_names::{BluetoothChannelName, Dot11ChannelName, EthernetChannelName};
use crate::metrics::ChannelUtilization;

use super::{payloads::{StatusReport, SystemMetricsReport, TotalWithAverage, ChannelReport, CaptureReport}};

pub struct Leaderlink {
    http_client: Client,
    uri: Url,
    system: System,
    metrics: Arc<Mutex<Metrics>>,
    ethernet_bus: Arc<Bus>,
    dot11_bus: Arc<Bus>,
    bluetooth_bus: Arc<Bus>
}

impl Leaderlink {
    pub fn new(configuration: Configuration,
               metrics: Arc<Mutex<Metrics>>,
               ethernet_bus: Arc<Bus>,
               dot11_bus: Arc<Bus>,
               bluetooth_bus: Arc<Bus>)
        -> anyhow::Result<Self, anyhow::Error> {

        let uri = match Url::parse(&configuration.general.leader_uri) {
            Ok(uri) => uri,
            Err(err) => bail!("Could not parse leader URI. {}", err)
        };

        let mut default_headers = HeaderMap::new();
        let bearer = "Bearer ".to_owned() + configuration.general.leader_secret.as_str();
        default_headers.insert(AUTHORIZATION, bearer.parse().unwrap());

        let http_client = reqwest::blocking::Client::builder()
            .timeout(Duration::from_secs(20))
            .user_agent("nzyme-tap")
            .default_headers(default_headers)
            .gzip(true)
            .danger_accept_invalid_certs(configuration.general.accept_insecure_certs) // TODO
            .build()?;

        anyhow::Ok(Self {
            http_client,
            uri,
            system: System::new(),
            metrics,
            ethernet_bus,
            dot11_bus,
            bluetooth_bus
        })
    }

    pub fn run(&mut self) {
        // Status report.
        match self.send_status() {
            Ok(r) => {
                if !r.status().is_success() {
                    error!("Could not send status. Received response code [HTTP {}].", r.status());
                }
            },
            Err(e) => {
                error!("Could not send status. {}", e);
            }
        };
    }

    pub fn send_node_hello(&self,
                           wifi_device_assignments: &Option<HashMap<String, Vec<SupportedFrequency>>>,
                           wifi_device_cycle_times: &Option<HashMap<String, u64>>)
        -> Result<(), anyhow::Error>{

        let mut wda_report = HashMap::new();
        if wifi_device_assignments.is_some() {
            for (adapter_name, freqs) in wifi_device_assignments.as_ref().unwrap() {
                let mut frequencies: Vec<WiFiSupportedFrequencyReport> = Vec::new();
                for freq in freqs {
                    let mut channel_widths: Vec<String> = Vec::new();

                    for width in &freq.channel_widths {
                        channel_widths.push(match width {
                            SupportedChannelWidth::Mhz20 => "20".to_string(),
                            SupportedChannelWidth::Mhz40Minus => "40MINUS".to_string(),
                            SupportedChannelWidth::Mhz40Plus => "40PLUS".to_string(),
                            SupportedChannelWidth::Mhz80 => "80".to_string(),
                            SupportedChannelWidth::Mhz160 => "160".to_string(),
                            SupportedChannelWidth::Mhz320 => "320".to_string(),
                        });
                    }

                    frequencies.push(WiFiSupportedFrequencyReport {
                        frequency: freq.frequency,
                        channel_widths
                    })
                }

                wda_report.insert(adapter_name.clone(), frequencies);
            }
        }

        let mut ct_report = HashMap::new();
        if wifi_device_cycle_times.is_some() {
            for (adapter_name, cycle_time) in wifi_device_cycle_times.as_ref().unwrap() {
                ct_report.insert(adapter_name.clone(), *cycle_time);
            }
        }

        let report = NodeHelloReport {
            wifi_device_assignments: wda_report,
            wifi_device_cycle_times: ct_report
        };

        let mut uri = self.uri.clone();
        uri.set_path("/api/taps/hello");

        match self.http_client
            .post(uri)
            .header("Content-Type", "application/json")
            .json(&report)
            .send() {
                Ok(_) => Ok(()),
                Err(e) => bail!("HTTP POST failed: {}", e)
        }
    }

    pub fn send_report(&self, path: &str, report: String) -> Result<Response, anyhow::Error> {
        let mut uri = self.uri.clone();
        uri.set_path(format!("/api/taps/tables/{}", path).as_str());

        match self.http_client
            .post(uri)
            .header("Content-Type", "application/json")
            .body(report.clone())
            .send() {
            Ok(r) => {
                if !r.status().is_success() {
                    bail!("Could not send report. Received response code [HTTP {}].", r.status())
                } else {
                    Ok(r)
                }
            },
            Err(e) => bail!("Could not send report. {}", e)
        }
    }

    fn send_status(&mut self) -> Result<Response, Error> {
        let mut processed_bytes_total =0;
        let mut processed_bytes_avg = 0;
        let mut ethernet_channels: Vec<ChannelReport> = Vec::new();
        let mut dot11_channels: Vec<ChannelReport> = Vec::new();
        let mut bluetooth_channels: Vec<ChannelReport> = Vec::new();
        let mut captures: Vec<CaptureReport> = Vec::new();
        let mut gauges_long: HashMap<String, i128> = HashMap::new();
        let mut timers: HashMap<String, TimerReport> = HashMap::new();
        let mut log_counts: HashMap<String, u128> = HashMap::new();

        match self.metrics.lock() {
            Ok(mut metrics) => {
                processed_bytes_total = metrics.get_processed_bytes().total;
                processed_bytes_avg = metrics.get_processed_bytes().avg;

                // TODO de-duplicate here.
                for c in EthernetChannelName::iter() {
                    ethernet_channels.push(
                        Self::build_channel_report(metrics.select_channel(&c.to_string()), c.to_string())
                    );
                }

                for c in Dot11ChannelName::iter() {
                    dot11_channels.push(
                        Self::build_channel_report(metrics.select_channel(&c.to_string()), c.to_string())
                    );
                }

                for c in BluetoothChannelName::iter() {
                    bluetooth_channels.push(
                        Self::build_channel_report(metrics.select_channel(&c.to_string()), c.to_string())
                    );
                }

                for capture in metrics.get_captures().into_values() {
                    captures.push(CaptureReport {
                        capture_type: capture.capture_type.to_string(),
                        interface_name: capture.interface_name,
                        is_running: capture.is_running,
                        received: capture.received,
                        dropped_buffer: capture.dropped_buffer,
                        dropped_interface: capture.dropped_interface
                    });
                }

                gauges_long = metrics.get_gauges_long();

                for (name, timer) in metrics.get_timer_snapshots().iter() {
                    timers.insert(name.clone(), TimerReport {
                        mean: timer.mean,
                        p99: timer.p99
                    });
                }

                // Log counts.
                match metrics.get_log_counts() {
                    Ok(lc) => {
                        log_counts.insert("error".to_string(), lc.error);
                        log_counts.insert("warn".to_string(), lc.warn);
                        log_counts.insert("info".to_string(), lc.info);
                        log_counts.insert("debug".to_string(), lc.debug);
                        log_counts.insert("trace".to_string(), lc.trace);
                    },
                    Err(e) => error!("Could not get log counts: {}", e)
                }

                if let Err(e) = metrics.reset_log_counts() {
                    error!("Could not reset log counts: {}", e);
                }
            },
            Err(e) => {
                error!("Could not acquire metrics mutex. {}", e);
            }
        };

        let system_metrics = self.build_system_metrics();

        let status = StatusReport {
            version: env!("CARGO_PKG_VERSION").to_string(),
            timestamp: Utc::now(),
            processed_bytes: TotalWithAverage { 
                total: processed_bytes_total,
                average: processed_bytes_avg
            },
            buses: vec![
                super::payloads::BusReport {
                    channels: ethernet_channels,
                    name: self.ethernet_bus.name.clone()
                },
                super::payloads::BusReport {
                    channels: dot11_channels,
                    name: self.dot11_bus.name.clone()
                },
                super::payloads::BusReport {
                    channels: bluetooth_channels,
                    name: self.bluetooth_bus.name.clone()
                },
            ],
            system_metrics,
            captures,
            gauges_long,
            timers,
            log_counts
        };
        
        let mut uri = self.uri.clone();
        uri.set_path("/api/taps/status");

        self.http_client
            .post(uri)
            .json(&status)
            .send()
    }

    fn build_system_metrics(&self) -> SystemMetricsReport {
        let cpu_load: f32;
        match self.system.cpu_load_aggregate() {
            Ok(cpu) => {
                // Have to sleep for a brief moment to allow gathering of data.
                thread::sleep(Duration::from_secs(1));
                match cpu.done() {
                    Ok(cpu) => {
                        cpu_load = (cpu.user+cpu.nice+cpu.system+cpu.interrupt)*100.0; 
                    },
                    Err(e) => {
                        error!("Could not determine CPU load average. {}", e);
                        cpu_load = 0.0;
                    }
                }
            },
            Err(e) => {
                error!("Could not determine CPU load average. {}", e);
                cpu_load = 0.0;
            }
        }

        let memory_total: u64;
        let memory_free: u64;
        match self.system.memory() {
            Ok(mem) => {
                memory_total = mem.total.as_u64();
                memory_free = mem.free.as_u64();
            },
            Err(e) => {
                error!("Could not determine memory metrics. {}", e);
                memory_total = 0;
                memory_free = 0;
            }
        }

        SystemMetricsReport {
            cpu_load,
            memory_total,
            memory_free
        }
    }

    fn build_channel_report(metrics: &ChannelUtilization, name: String) -> ChannelReport {
        ChannelReport {
            name,
            capacity: metrics.capacity,
            watermark: metrics.watermark,
            errors: TotalWithAverage::from_metric(&metrics.errors),
            throughput_bytes: TotalWithAverage::from_metric(&metrics.throughput_bytes),
            throughput_messages: TotalWithAverage::from_metric(&metrics.throughput_messages)
        }
    }

}
