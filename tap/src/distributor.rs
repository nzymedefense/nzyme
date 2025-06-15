use std::{process::exit, sync::{Arc, Mutex}, thread};

use log::{error, info};

use crate::{exit_code, messagebus::bus::Bus, metrics::Metrics, system_state::SystemState};
use crate::wireless::bluetooth::processors::bluetooth_device_processor::BluetoothDeviceProcessor;
use crate::configuration::Configuration;
use crate::context::context_engine::ContextEngine;
use crate::protocols::processors::arp_processor::ARPProcessor;
use crate::protocols::processors::dhcpv4_processor::Dhcpv4Processor;
use crate::protocols::processors::dns_processor::DnsProcessor;
use crate::protocols::processors::dot11::dot11_frame_processor::Dot11FrameProcessor;
use crate::protocols::processors::socks_processor::SocksProcessor;
use crate::protocols::processors::ssh_processor::SshProcessor;
use crate::protocols::processors::tcp_processor::TcpProcessor;
use crate::protocols::processors::uav_remote_id_processor::UavRemoteIdProcessor;
use crate::protocols::processors::udp_processor::UDPProcessor;
use crate::state::state::State;
use crate::state::tables::tables::Tables;

pub fn spawn(ethernet_bus: Arc<Bus>,
             dot11_bus: Arc<Bus>,
             bluetooth_bus: Arc<Bus>,
             generic_bus: Arc<Bus>,
             tables: Arc<Tables>,
             state: Arc<State>,
             context: Arc<ContextEngine>,
             system_state: Arc<SystemState>,
             metrics: Arc<Mutex<Metrics>>,
             configuration: &Configuration) {
    spawn_base_dot11(dot11_bus.clone(), generic_bus.clone(), metrics.clone(), tables.clone());

    spawn_base_bluetooth(bluetooth_bus.clone(), tables.clone());

    spawn_base_arp(ethernet_bus.clone(), tables.clone(), state.clone(), context.clone());
    spawn_base_dhcpv4(ethernet_bus.clone(), tables.clone(), state.clone(), context.clone());
    
    spawn_base_tcp(ethernet_bus.clone(), tables.clone(), context.clone());
    spawn_base_udp(ethernet_bus.clone(), tables.clone(), metrics.clone(), context.clone());
    
    spawn_base_dns(ethernet_bus.clone(), 
                   tables.clone(), 
                   context.clone(),
                   state.clone(), 
                   system_state, 
                   metrics.clone(),
                   configuration);
    spawn_base_ssh(ethernet_bus.clone(), tables.clone(), metrics.clone());
    spawn_base_socks(ethernet_bus.clone(), tables.clone(), metrics.clone());
    
    spawn_base_uav_remote_id(generic_bus.clone(), tables.clone());
}

// TODO don't exit here ever

fn spawn_base_dot11(dot11_bus: Arc<Bus>, generic_bus: Arc<Bus>, metrics: Arc<Mutex<Metrics>>, tables: Arc<Tables>) {
    let processor = Dot11FrameProcessor::new(
        tables.dot11.clone(), metrics, generic_bus
    );

    thread::spawn(move || {
        for frame in dot11_bus.dot11_frames_pipeline.receiver.iter() {
            processor.process(frame);
        }

        error!("802.11 frames receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });
}

fn spawn_base_bluetooth(bus: Arc<Bus>, tables: Arc<Tables>) {
    let processor = BluetoothDeviceProcessor::new(tables.bluetooth.clone());

    thread::spawn(move || {
        for device in bus.bluetooth_device_pipeline.receiver.iter() {
            processor.process(device);
        }

        error!("Bluetooth receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });
}

fn spawn_base_arp(bus: Arc<Bus>,
                  tables: Arc<Tables>,
                  state: Arc<State>,
                  context: Arc<ContextEngine>) {
    let mut processor = ARPProcessor::new(tables, state.clone(), context);
    thread::spawn(move || {
        for packet in bus.arp_pipeline.receiver.iter() {
            processor.process(packet);
        }

        error!("ARP receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });
}

fn spawn_base_dhcpv4(bus: Arc<Bus>, 
                     tables: Arc<Tables>,
                     state: Arc<State>, 
                     context: Arc<ContextEngine>) {
    let mut processor = Dhcpv4Processor::new(state, tables.dhcp.clone(), context);

    thread::spawn(move || {
        for packet in bus.dhcpv4_pipeline.receiver.iter() {
            processor.process(packet);
        }

        error!("DHCPv4 receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });
}

fn spawn_base_tcp(bus: Arc<Bus>, tables: Arc<Tables>, context: Arc<ContextEngine>) {
    let mut processor = TcpProcessor::new(tables.tcp.clone(), context);

    thread::spawn(move || {
        for segment in bus.tcp_pipeline.receiver.iter() {
            processor.process(segment);
        }

        error!("TCP receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });
}

fn spawn_base_udp(bus: Arc<Bus>, tables: Arc<Tables>, metrics: Arc<Mutex<Metrics>>, context: Arc<ContextEngine>) {
    let mut processor = UDPProcessor::new(
        bus.clone(), metrics.clone(), tables.udp.clone(), context
    );

    thread::spawn(move || {
        for datagram in bus.udp_pipeline.receiver.iter() {
            processor.process(datagram);
        }

        error!("UDP receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });
}

fn spawn_base_dns(bus: Arc<Bus>,
                  tables: Arc<Tables>,
                  context_engine: Arc<ContextEngine>,
                  state: Arc<State>,
                  system_state: Arc<SystemState>,
                  metrics: Arc<Mutex<Metrics>>,
                  configuration: &Configuration) {
    let mut processor = DnsProcessor::new(system_state, 
                                          state.clone(), 
                                          context_engine.clone(),
                                          tables.dns.clone(), 
                                          metrics, 
                                          configuration);

    thread::spawn(move || {
        for packet in bus.dns_pipeline.receiver.iter() {
            processor.process(packet);
        }

        error!("DNS receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });
}

fn spawn_base_ssh(bus: Arc<Bus>,
                  tables: Arc<Tables>,
                  metrics: Arc<Mutex<Metrics>>) {
    let mut processor = SshProcessor::new(metrics.clone(), tables.ssh.clone());

    thread::spawn(move || {
        for packet in bus.ssh_pipeline.receiver.iter() {
            processor.process(packet);
        }

        error!("SSH receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });
}

fn spawn_base_socks(bus: Arc<Bus>,
                    tables: Arc<Tables>,
                    metrics: Arc<Mutex<Metrics>>) {
    let mut processor = SocksProcessor::new(metrics.clone(), tables.socks.clone());

    thread::spawn(move || {
        for packet in bus.socks_pipeline.receiver.iter() {
            processor.process(packet);
        }

        error!("SOCKS receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });
}

fn spawn_base_uav_remote_id(bus: Arc<Bus>, tables: Arc<Tables>) {
    let mut processor = UavRemoteIdProcessor::new(tables.uav.clone());

    thread::spawn(move || {
        for segment in bus.uav_remote_id_pipeline.receiver.iter() {
            processor.process(segment);
        }

        error!("UAV Remote ID receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });
}