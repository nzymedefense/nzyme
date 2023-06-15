use std::{sync::{Arc, Mutex}, thread, process::exit};

use log::error;

use crate::{messagebus::bus::Bus, exit_code, data::tables::Tables, system_state::SystemState, metrics::Metrics};

use super::{
    arp_processor::ARPProcessor,
    dns_processor::DnsProcessor,
    udp_processor::UDPProcessor, 
    tcp_processor::TCPProcessor, dot11_frame_processor::Dot11FrameProcessor,
};

pub fn spawn(bus: Arc<Bus>, tables: &Arc<Tables>, system_state: Arc<SystemState>, metrics: Arc<Mutex<Metrics>>) {
    spawn_base_ethernet(bus.clone());
    spawn_base_dot11_management(bus.clone(), &tables.clone());

    spawn_base_arp(bus.clone(), tables.clone()); // TODO borrow
    
    spawn_base_tcp(bus.clone(), &tables.clone());
    spawn_base_udp(bus.clone(), &tables.clone());    
    
    spawn_base_dns(bus, tables, system_state, metrics);    
}

// TODO don't exit here

fn spawn_base_ethernet(bus: Arc<Bus>) {
    thread::spawn(move || {
        for _packet in bus.ethernet_pipeline.receiver.iter() {
            // noop
        }

        error!("Ethernet receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });
}

fn spawn_base_dot11_management(bus: Arc<Bus>, tables: &Arc<Tables>) {
    let processor = Dot11FrameProcessor::new(tables.dot11_networks.clone());

    thread::spawn(move || {
        for frame in bus.dot11_frames_pipeline.receiver.iter() {
            processor.process(&frame);
        }

        error!("Dot11 frames receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });
}

fn spawn_base_arp(bus: Arc<Bus>, tables: Arc<Tables>) {
    let mut processor = ARPProcessor::new(tables);
    thread::spawn(move || {
        for packet in bus.arp_pipeline.receiver.iter() {
            processor.process(&packet);
        }

        error!("ARP receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });
}

fn spawn_base_tcp(bus: Arc<Bus>, tables: &Arc<Tables>) {
    let mut processor = TCPProcessor::new(tables.l4.clone());
    thread::spawn(move || {
        for packet in bus.tcp_pipeline.receiver.iter() {
            processor.process(&packet);
        }

        error!("TCP receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });
}

fn spawn_base_udp(bus: Arc<Bus>, tables: &Arc<Tables>) {
    let mut processor = UDPProcessor::new(tables.l4.clone());
    thread::spawn(move || {
        for packet in bus.udp_pipeline.receiver.iter() {
            processor.process(&packet);
        }

        error!("UDP receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });

}

fn spawn_base_dns(bus: Arc<Bus>, tables: &Arc<Tables>, system_state: Arc<SystemState>, metrics: Arc<Mutex<Metrics>>) {
    let mut processor = DnsProcessor::new(system_state, tables.dns.clone(), metrics);

    thread::spawn(move || {
        for packet in bus.dns_pipeline.receiver.iter() {
            processor.process(&packet);
        }

        error!("DNS receiver disconnected.");
        exit(exit_code::EX_UNAVAILABLE);
    });

}
