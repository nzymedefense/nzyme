use std::{thread, time::Duration, sync::{Mutex, Arc}, collections::HashMap};
use strum::IntoEnumIterator;
use strum_macros::Display;

use log::{warn, error};

use crate::messagebus::channel_names::ChannelName;

#[derive(Default, Clone)]
pub struct TotalWithAverage {
    pub avg: u128,
    avg_tmp: u128,
    pub total: u128
}

impl TotalWithAverage {

    pub fn increment(&mut self, x: u32) {
        self.total += u128::from(x);
        self.avg_tmp += u128::from(x);
    }

    pub fn calculate_averages(&mut self) {
        self.avg = self.avg_tmp;
        self.avg_tmp = 0;
    }

}

#[derive(Default)]
pub struct ChannelUtilization {
    pub watermark: u128,
    pub errors: TotalWithAverage,
    pub throughput_bytes: TotalWithAverage,
    pub throughput_messages: TotalWithAverage,
    pub capacity: u128
}

pub struct Channels {
    ethernet_broker: ChannelUtilization,
    dot11_broker: ChannelUtilization,

    dot11_frames_pipeline: ChannelUtilization,

    ethernet_pipeline: ChannelUtilization,
    arp_pipeline: ChannelUtilization,
    tcp_pipeline: ChannelUtilization,
    udp_pipeline: ChannelUtilization,
    dns_pipeline: ChannelUtilization,
}

#[derive(Clone, Display)]
pub enum CaptureType {
    Ethernet,
    WiFi
}

#[derive(Clone)]
pub struct Capture {
    pub capture_type: CaptureType,
    pub interface_name: String,
    pub is_running: bool,
    pub received: u32,
    pub dropped_buffer: u32,
    pub dropped_interface: u32
}

pub const AVERAGE_INTERVAL: u8 = 10;

pub struct Metrics {
    captures: HashMap<String, Capture>,
    processed_bytes: TotalWithAverage,
    channels: Channels,
    gauges_long: HashMap<String, i128>
}

impl Metrics {

    pub fn new() -> Self {
        Metrics {
            processed_bytes: TotalWithAverage::default(),
            channels: Channels {
                ethernet_broker: ChannelUtilization::default(),
                dot11_broker: ChannelUtilization::default(),
                dot11_frames_pipeline: ChannelUtilization::default(),
                ethernet_pipeline: ChannelUtilization::default(),
                arp_pipeline: ChannelUtilization::default(),
                tcp_pipeline: ChannelUtilization::default(),
                udp_pipeline: ChannelUtilization::default(),
                dns_pipeline: ChannelUtilization::default(),
            },
            captures: HashMap::new(),
            gauges_long: HashMap::new()
        }
    }

    pub fn calculate_averages(&mut self) {
        self.processed_bytes.calculate_averages();

        for channel in ChannelName::iter() {
            let c = self.select_channel(&channel.clone());
            c.throughput_bytes.calculate_averages();
            c.throughput_messages.calculate_averages();
            c.errors.calculate_averages();
        }
    }
    
    pub fn register_new_capture(&mut self, name: &str, capture_type: CaptureType) {
        if self.captures.contains_key(name) {
            error!("Attempt to re-register new capture. Ignoring.");
        } else {
            let sname = name.to_string();
            self.captures.insert(sname.clone(), Capture {
                capture_type,
                interface_name: sname,
                is_running: false,
                received: 0,
                dropped_buffer: 0,
                dropped_interface: 0
            });
        }
    }

    pub fn update_capture(&mut self, name: &str, is_running: bool, dropped_buffer: u32, dropped_interface: u32) {
        if self.captures.contains_key(name) {
           let previous = self.captures.get(name).unwrap();

           // Update capture.
           self.captures.insert(name.to_string(), Capture {
                capture_type: previous.capture_type.clone(),
                interface_name: previous.interface_name.clone(),
                is_running,
                received: previous.received + 1,
                dropped_buffer,
                dropped_interface
            });
        } else {
            error!("Capture [{}] not found during attempted metric update. Ignoring.", name);
        }
    }

    pub fn mark_capture_as_failed(&mut self, name: &str) {
        if self.captures.contains_key(name) {
            let previous = self.captures.get(name).unwrap();
 
            // Update capture.
            self.captures.insert(name.to_string(), Capture {
                 capture_type: previous.capture_type.clone(),
                 interface_name: previous.interface_name.clone(),
                 is_running: false,
                 received: previous.received,
                 dropped_buffer: previous.dropped_buffer,
                 dropped_interface: previous.dropped_interface
             });
         } else {
            error!("Capture [{}] not found during attempted metric update. Ignoring.", name);
         }
    }

    pub fn select_channel(&mut self, channel: &ChannelName) -> &mut ChannelUtilization {
        match channel {
            ChannelName::EthernetBroker => &mut self.channels.ethernet_broker,
            ChannelName::Dot11Broker => &mut self.channels.dot11_broker,
            ChannelName::Dot11FramesPipeline => &mut self.channels.dot11_frames_pipeline,
            ChannelName::EthernetPipeline => &mut self.channels.ethernet_pipeline,
            ChannelName::ArpPipeline => &mut self.channels.arp_pipeline,
            ChannelName::TcpPipeline => &mut self.channels.tcp_pipeline,
            ChannelName::UdpPipeline => &mut self.channels.udp_pipeline,
            ChannelName::DnsPipeline => &mut self.channels.dns_pipeline
        }
    }

    pub fn increment_channel_errors(&mut self, channel: &ChannelName, x: u32) {
        self.select_channel(channel).errors.increment(x);
    }

    pub fn increment_channel_throughput_bytes(&mut self, channel: &ChannelName, x: u32) {
        self.select_channel(channel).throughput_bytes.increment(x);
    }
    
    pub fn increment_channel_throughput_messages(&mut self, channel: &ChannelName, x: u32) {
        self.select_channel(channel).throughput_messages.increment(x);
    }

    pub fn record_channel_watermark(&mut self, channel: &ChannelName, watermark: u128) {
        self.select_channel(channel).watermark = watermark;
    }
    
    pub fn record_channel_capacity(&mut self, channel: &ChannelName, capacity: u128) {
        self.select_channel(channel).capacity = capacity;
    }

    pub fn increment_processed_bytes_total(&mut self, x: u32) {
        self.processed_bytes.increment(x);
    }

    pub fn set_gauge(&mut self, name: &str, value: i128) {
        self.gauges_long.insert(name.to_string(), value);
    }

    /*
     * GETTERS
     */

    pub fn get_processed_bytes(&self) -> TotalWithAverage {
        self.processed_bytes.clone()
    }

    pub fn get_channel_errors(&mut self, channel: &ChannelName) -> TotalWithAverage {
        self.select_channel(channel).errors.clone()
    }

    pub fn get_captures(&self) -> HashMap<String, Capture> {
        let mut cloned = HashMap::new();
        cloned.clone_from(&self.captures);

        cloned
    }

    pub fn get_gauges_long(&self) -> HashMap<String, i128> {
        let mut cloned = HashMap::new();
        cloned.clone_from(&self.gauges_long);

        cloned
    }
}

pub struct MetricsMonitor {
    metrics: Arc<Mutex<Metrics>>
}

impl MetricsMonitor {

    pub fn new(metrics: Arc<Mutex<Metrics>>) -> Self {
        Self { metrics }
    }

    pub fn run(&mut self) {
        loop {
            thread::sleep(Duration::from_secs(AVERAGE_INTERVAL.try_into().unwrap()));

            match self.metrics.lock() {
                Ok(mut metrics) => {
                    for channel in ChannelName::iter() {
                        let errors = metrics.get_channel_errors(&channel.clone()).avg;

                        if errors > 0 {
                            error!("Channel [{:?}] had <{}> submit errors in last <{}> seconds. You are losing packets.", 
                                channel, errors, AVERAGE_INTERVAL);
                        }
                    }
                },
                Err(e) => error!("Could not acquire metrics mutex: {}", e)
            }
        }
    }

}

pub struct MetricsAggregator {
    metrics: Arc<Mutex<Metrics>>
}

impl MetricsAggregator {

    pub fn new(metrics: Arc<Mutex<Metrics>>) -> Self {
        Self { metrics }
    }

    pub fn run(&mut self) {
        loop {
            match self.metrics.lock() {
                Ok(mut metrics) => {
                    metrics.calculate_averages();
                },
                Err(e) => { warn!("Could not acquire metrics mutex in aggregator: {}", e) }
            }

            thread::sleep(Duration::from_secs(AVERAGE_INTERVAL.try_into().unwrap()));
        }
    }

}
