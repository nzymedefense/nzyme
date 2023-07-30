mod ethernet;
mod helpers;
mod brokers;
mod messagebus;
mod link;
mod configuration;
mod exit_code;
mod metrics;
mod processors;
mod data;
mod system_state;
mod logging;
mod dot11;

use std::{time, thread::{self, sleep}, time::Duration, sync::{Arc, Mutex}, process::exit};

use clap::Parser;
use configuration::Configuration;
use data::tables::Tables;
use link::leaderlink::Leaderlink;
use log::{error, info};
use messagebus::bus::Bus;
use system_state::SystemState;

use crate::dot11::{channel_hopper::ChannelHopper};

#[derive(Parser,Debug)]
struct Arguments {
    #[clap(short, long, forbid_empty_values = true)]
    configuration_file: String,

    #[clap(short, long, forbid_empty_values = true)]
    log_level: String
}

fn main() {    
    let args = Arguments::parse();

    logging::initialize(&args.log_level);

    info!("Starting nzyme tap version [{}].", env!("CARGO_PKG_VERSION"));

    // Load configuration.
    let configuration: Configuration = match configuration::load(args.configuration_file) {
        Ok(configuration) => {
            info!("Parsed and loaded configuration.");
            configuration
        },
        Err(e) => {
            error!("Fatal error: Could not load configuration. {}", e);
            exit(exit_code::EX_CONFIG);
        }
    };
    
    let system_state = Arc::new(
        SystemState::new(configuration.misc.training_period_minutes as usize).initialize()
    );

    ethernet::capture::print_devices();
    
    let metrics = Arc::new(Mutex::new(metrics::Metrics::new()));
    let bus = Arc::new(Bus::new(metrics.clone(), "ethernet_packets".to_string()));
    let tables = Arc::new(Tables::new(metrics.clone()));

    let tables_bg = tables.clone();
    thread::spawn(move || {
        tables_bg.run_background_jobs();
    });

    // Ethernet handler.
    let ethernet_handlerbus = bus.clone();
    thread::spawn(move || {
        brokers::ethernet_broker::EthernetBroker::new(ethernet_handlerbus, configuration.performance.ethernet_brokers as usize).run();
    });

    // WiFi handler.
    let wifi_handlerbus = bus.clone();
    thread::spawn(move || {
        brokers::dot11_broker::Dot11Broker::new(wifi_handlerbus, configuration.performance.wifi_brokers as usize).run();
    });

    // Ethernet Capture.
    let ethernet_capture_conf = configuration.clone();
    if let Some(ethernet_interfaces) = ethernet_capture_conf.ethernet_interfaces {
        for (interface_name, interface_config) in ethernet_interfaces {
            if !interface_config.active {
                info!("Skipping disabled WiFi interface [{}].", interface_name);
                continue;
            }
            let capture_metrics = metrics.clone();
            let capture_bus = bus.clone();
            thread::spawn(move || {
                let mut ethernet_capture = ethernet::capture::Capture {
                    metrics: capture_metrics.clone(),
                    bus: capture_bus
                };

                match capture_metrics.lock() {
                    Ok(mut metrics) => metrics.register_new_capture(&interface_name, metrics::CaptureType::Ethernet),
                    Err(e) => error!("Could not acquire mutex of metrics: {}", e)
                }

                loop {
                    ethernet_capture.run(&interface_name);

                    error!("Ethernet capture [{}] disconnected. Retrying in 5 seconds.", interface_name);
                    match capture_metrics.lock() {
                        Ok(mut metrics) => metrics.mark_capture_as_failed(&interface_name),
                        Err(e) => error!("Could not acquire mutex of metrics: {}", e)
                    }
                    thread::sleep(time::Duration::from_secs(5));
                }
            });
        }
    }

    // WiFi capture.
    let wifi_capture_conf = configuration.clone();
    if let Some(wifi_interfaces) = wifi_capture_conf.wifi_interfaces {
        for (interface_name, interface_config) in wifi_interfaces {
            if !interface_config.active {
                info!("Skipping disabled WiFi interface [{}].", interface_name);
                continue;
            }

            let capture_metrics = metrics.clone();
            let capture_bus = bus.clone();
            thread::spawn(move || {
                let mut dot11_capture = dot11::capture::Capture {
                    metrics: capture_metrics.clone(),
                    bus: capture_bus.clone()
                };

                match capture_metrics.lock() {
                    Ok(mut metrics) => metrics.register_new_capture(&interface_name, metrics::CaptureType::WiFi),
                    Err(e) => error!("Could not acquire mutex of metrics: {}", e)
                }

                loop {
                    dot11_capture.run(&interface_name);

                    error!("WiFi capture [{}] disconnected. Retrying in 5 seconds.", &interface_name);
                    match capture_metrics.lock() {
                        Ok(mut metrics) => metrics.mark_capture_as_failed(&interface_name),
                        Err(e) => error!("Could not acquire mutex of metrics: {}", e)
                    }
                    thread::sleep(time::Duration::from_secs(5));
                }
            });
        }
    }

    if let Some(wifi_interfaces) = configuration.clone().wifi_interfaces {
        let ch = match ChannelHopper::new(wifi_interfaces) {
            Ok(ch) => ch,
            Err(e) => {
                error!("Could not initialize ChannelHopper: {}", e);
                exit(exit_code::EX_OSERR);
            }
        };
        ch.spawn_loop();
    }

    // Processors. TODO follow impl method like metrics aggr/mon
    processors::distributor::spawn(bus.clone(), &tables, system_state, metrics.clone());

    // Metrics aggregator.
    let aggregatormetrics = metrics.clone();
    thread::spawn(move || {
        metrics::MetricsAggregator::new(aggregatormetrics).run();
    });
    
    // Metrics monitor.
    let monitormetrics = metrics.clone();
    thread::spawn(move || {
        metrics::MetricsMonitor::new(monitormetrics).run();
    });

    thread::spawn(move || { // TODO capsule into struct
        let mut leaderlink = match Leaderlink::new(configuration, metrics, bus, tables) {
            Ok(leaderlink) => leaderlink,
            Err(e) => {
                error!("Fatal error: Could not set up conection to nzyme leader. {}", e);
                exit(exit_code::EX_CONFIG);
            }
        };

        leaderlink.run();
    });

    info!("Bootstrap complete.");

    loop {
        sleep(Duration::from_secs(1));
    }

}
