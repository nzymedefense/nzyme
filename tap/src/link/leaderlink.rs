use anyhow::bail;
use chrono::Utc;
use log::{error, info};
use reqwest::{blocking::Client, blocking::Response, header::{HeaderMap, AUTHORIZATION}, Error, Url};
use std::{thread::{sleep, self}, time::Duration, sync::{Mutex, Arc}, collections::HashMap};
use systemstat::{System, Platform};
use strum::IntoEnumIterator;

use crate::{
    configuration::Configuration,
    metrics::Metrics, data::tables::Tables, messagebus::{channel_names::ChannelName, bus::Bus}, link::payloads::{DnsTableReport, Dot11TableReport}
};
use crate::link::payloads::{Dot11DiscoReport, TimerReport};

use super::{payloads::{StatusReport, SystemMetricsReport, TablesReport, TotalWithAverage, ChannelReport, CaptureReport}};


pub struct Leaderlink {
    http_client: Client,
    uri: Url,
    system: System,
    metrics: Arc<Mutex<Metrics>>,
    bus: Arc<Bus>,
}

impl Leaderlink {
    pub fn new(configuration: Configuration, metrics: Arc<Mutex<Metrics>>, bus: Arc<Bus>) -> anyhow::Result<Self, anyhow::Error> {
        let uri = match Url::parse(&configuration.general.leader_uri) {
            Ok(uri) => uri,
            Err(err) => bail!("Could not parse leader URI. {}", err)
        };

        let mut default_headers = HeaderMap::new();
        let bearer = "Bearer ".to_owned() + configuration.general.leader_secret.as_str();
        default_headers.insert(AUTHORIZATION, bearer.parse().unwrap());

        let http_client = reqwest::blocking::Client::builder()
            .timeout(Duration::from_secs(10))
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
            bus
        })
    }

    pub fn run(&mut self) {
        // Status report.
        match self.send_status() {
            Ok(r) => {
                if !r.status().is_success() {
                    error!("Could not send status to leader. Received response code [HTTP {}].", r.status());
                }
            },
            Err(e) => {
                error!("Could not send status to leader. {}", e);
            }
        };
    }

    pub fn send_report(&self, path: &str, report: String) -> Result<Response, Error> {
        let mut uri = self.uri.clone();
        uri.set_path(format!("/api/taps/tables/{}", path).as_str());

        self.http_client
            .post(uri)
            .header("Content-Type", "application/json")
            .body(report.clone())
            .send()
    }

    fn send_status(&mut self) -> Result<Response, Error> {
        let mut processed_bytes_total =0;
        let mut processed_bytes_avg = 0;
        let mut channels: Vec<ChannelReport> = Vec::new();
        let mut captures: Vec<CaptureReport> = Vec::new();
        let mut gauges_long: HashMap<String, i128> = HashMap::new();
        let mut timers: HashMap<String, TimerReport> = HashMap::new();

        match self.metrics.lock() {
            Ok(mut metrics) => {
                processed_bytes_total = metrics.get_processed_bytes().total;
                processed_bytes_avg = metrics.get_processed_bytes().avg;

                for channel in ChannelName::iter() {
                    let c = metrics.select_channel(&channel);
                    channels.push(ChannelReport {
                        name: channel.to_string(),
                        capacity: c.capacity,
                        watermark: c.watermark,
                        errors: TotalWithAverage::from_metric(&c.errors),
                        throughput_bytes: TotalWithAverage::from_metric(&c.throughput_bytes),
                        throughput_messages: TotalWithAverage::from_metric(&c.throughput_messages)
                    });
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
            bus: super::payloads::BusReport { channels, name: self.bus.name.clone() },
            system_metrics,
            captures,
            gauges_long,
            timers
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

}
