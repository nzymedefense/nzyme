use anyhow::bail;
use chrono::Utc;
use log::error;
use reqwest::{blocking::Client, blocking::Response, header::{HeaderMap, AUTHORIZATION}, Error, Url};
use std::{thread::{sleep, self}, time::Duration, sync::{Mutex, Arc}, collections::HashMap};
use systemstat::{System, Platform};
use strum::IntoEnumIterator;

use crate::{
    configuration::Configuration,
    metrics::Metrics, data::tables::Tables, messagebus::{channel_names::ChannelName, bus::Bus}, link::payloads::{DnsTableReport, L4TableReport, Dot11TableReport}
};

use super::{payloads::{StatusReport, SystemMetricsReport, TablesReport, TotalWithAverage, ChannelReport, CaptureReport}};


pub struct Leaderlink {
    http_client: Client,
    uri: Url,
    system: System,
    metrics: Arc<Mutex<Metrics>>,
    bus: Arc<Bus>,
    tables: Arc<Tables>
}

impl Leaderlink {
    pub fn new(configuration: Configuration, metrics: Arc<Mutex<Metrics>>, bus: Arc<Bus>, tables: Arc<Tables>) -> anyhow::Result<Self, anyhow::Error> {
        let uri = match Url::parse(&configuration.general.leader_uri) {
            Ok(uri) => uri,
            Err(err) => bail!("Could not parse leader URI. {}", err)
        };

        let mut default_headers = HeaderMap::new();
        let bearer = "Bearer ".to_owned() + &configuration.general.leader_secret;
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
            bus,
            tables
        })
    }

    pub fn run(&mut self) {
        loop {
            sleep(Duration::from_secs(10));

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

            // Tables.
            match self.send_tables() {
                Ok(r) => {
                    if !r.status().is_success() {
                        error!("Could not send tables to leader. Received response code [HTTP {}].", r.status());
                    }
                },
                Err(e) => {
                    error!("Could not send tables to leader. {}", e);
                }
            };

            /*
             * Clear tables after submission attempt. We might lose some data that was written between
             * the send attempt and the receipt, but that is OK considering the nature of the data and
             * the significantly more complex implementation required to guarantuee no data loss.
             * 
             * We are also accepting that data is lost when the submission failed. For now, this is
             * easier than implementing a robust and disk-persistent retry mechanism.
            */
            self.tables.clear_ephemeral();
        }
    }

    fn send_status(&mut self) -> Result<Response, Error> {
        let mut processed_bytes_total =0;
        let mut processed_bytes_avg = 0;
        let mut channels: Vec<ChannelReport> = Vec::new();
        let mut captures: Vec<CaptureReport> = Vec::new();
        let mut gauges_long: HashMap<String, i128> = HashMap::new();

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
            gauges_long
        };
        
        let mut uri = self.uri.clone();
        uri.set_path("/api/taps/status");

        self.http_client
            .post(uri)
            .json(&status)
            .send()
    }

    fn send_tables(&mut self) -> Result<Response, Error> {
        let mut uri = self.uri.clone();
        uri.set_path("/api/taps/tables");

        let arp = match self.tables.arp.lock() {
            Ok(table) => table.clone(),
            Err(e) => {
                error!("Could not aquire ARP table mutex. {}", e);
                HashMap::new()
            }
        };

        let dns = match self.tables.dns.lock() {
            Ok(dns) => dns.to_report(),
            Err(e) => {
                error!("Could not aquire DNS table mutex. {}", e);
                DnsTableReport {
                    ips: HashMap::new(),
                    nxdomains: Vec::new(),
                    entropy_log: Vec::new(),
                    pairs: HashMap::new(),
                    retro_queries: Vec::new(),
                    retro_responses: Vec::new()
                }
            }
        };

        let l4 = match self.tables.l4.lock() {
            Ok(mut l4) => l4.to_report(),
            Err(e) => {
                error!("Could not acquire L4 table mutex. {}", e);
                L4TableReport {
                    retro_pairs: Vec::new(),
                }
            }
        };

        let dot11 = match self.tables.dot11.lock() {
            Ok(dot11) => dot11.to_report(),
            Err(e) => {
                error!("Could not acquire 802.11 networks table mutex. {}", e);
                Dot11TableReport {
                    bssids: HashMap::new(),
                    clients: HashMap::new()
                }
            }
        };

        let tables = TablesReport {
            timestamp: Utc::now(),
            arp,
            dns,
            l4,
            dot11
        };

        self.http_client
            .post(uri)
            .json(&tables)
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
