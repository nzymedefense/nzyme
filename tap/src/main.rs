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
        SystemState::new(configuration.training_period_minutes as usize).initialize()
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
    let handlerbus = bus.clone();
    thread::spawn(move || {
        brokers::ethernet_broker::EthernetBroker::new(handlerbus, configuration.ethernet_brokers as usize).run();
    });

    // Ethernet Capture.
    for interface in configuration.clone().ethernet_listen_interfaces {
        let capturemetrics = metrics.clone();
        let capture_bus = bus.clone();
        thread::spawn(move || {
            let mut ec = ethernet::capture::Capture {
                metrics: capturemetrics.clone(),
                bus: capture_bus
            };

            match capturemetrics.lock() {
                Ok(mut metrics) => metrics.register_new_capture(interface.clone(), metrics::CaptureType::Ethernet),
                Err(e) => error!("Could not aquire mutex of metrics: {}", e)
            }

            loop {
                ec.run(&interface);

                error!("Ethernet capture [{}] disconnected. Retrying in 5 seconds.", interface);
                match capturemetrics.lock() {
                    Ok(mut metrics) => metrics.mark_capture_as_failed(&interface),
                    Err(e) => error!("Could not aquire mutex of metrics: {}", e)
                }
                thread::sleep(time::Duration::from_secs(5));
            }
        });
    }

    // WiFi capture. TODO load from config
    /*let mut dot11cap = dot11::capture::Capture {
        metrics: metrics.clone(),
        bus: bus.clone()
    };

    let dot11metrics = metrics.clone();
    thread::spawn(move || {
        loop {
            dot11cap.run("wlx9cefd5fd8c3e".to_string());

            error!("WiFi capture [TODO TODO TODO] disconnected. Retrying in 5 seconds."); // TODO
            match dot11metrics.lock() {
                Ok(mut metrics) => metrics.mark_capture_as_failed("wlx9cefd5fd8c3e".to_string()), // TODO
                Err(e) => error!("Could not aquire mutex of metrics: {}", e)
            }
            thread::sleep(time::Duration::from_secs(5));
        }
    });*/
    
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
