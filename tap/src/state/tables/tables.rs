use std::{
    collections::HashMap,
    sync::{Arc, Mutex}, thread
};
use std::net::IpAddr;
use log::error;
use std::time::Duration;
use crate::wireless::bluetooth::tables::bluetooth_table::BluetoothTable;
use crate::configuration::Configuration;
use crate::state::tables::dot11_table::Dot11Table;
use crate::state::tables::dns_table::DnsTable;
use crate::state::tables::socks_table::SocksTable;
use crate::state::tables::ssh_table::SshTable;
use crate::state::tables::tcp_table::TcpTable;
use crate::state::tables::udp_table::UdpTable;
use crate::link::leaderlink::Leaderlink;
use crate::messagebus::bus::Bus;

use crate::metrics::Metrics;
use crate::state::tables::uav_table::UavTable;

pub struct Tables {
    pub dot11: Arc<Mutex<Dot11Table>>,
    pub bluetooth: Arc<Mutex<BluetoothTable>>,
    pub arp: Arc<Mutex<HashMap<IpAddr, HashMap<String, u128>>>>,
    pub tcp: Arc<Mutex<TcpTable>>,
    pub udp: Arc<Mutex<UdpTable>>,
    pub dns: Arc<Mutex<DnsTable>>,
    pub ssh: Arc<Mutex<SshTable>>,
    pub socks: Arc<Mutex<SocksTable>>,
    pub uav: Arc<Mutex<UavTable>>
}

impl Tables {

    pub fn new(metrics: Arc<Mutex<Metrics>>,
               leaderlink: Arc<Mutex<Leaderlink>>,
               ethernet_bus: Arc<Bus>,
               configuration: &Configuration) -> Self {
        Tables {
            dot11: Arc::new(Mutex::new(Dot11Table::new(leaderlink.clone()))),
            bluetooth: Arc::new(Mutex::new(BluetoothTable::new(metrics.clone(), leaderlink.clone()))),
            arp: Arc::new(Mutex::new(HashMap::new())),
            dns: Arc::new(Mutex::new(DnsTable::new(metrics.clone(), leaderlink.clone()))),
            tcp: Arc::new(Mutex::new(TcpTable::new(
                leaderlink.clone(),
                ethernet_bus.clone(),
                metrics.clone(),
                configuration.protocols.tcp.reassembly_buffer_size,
                configuration.protocols.tcp.session_timeout_seconds
            ))),
            udp: Arc::new(Mutex::new(UdpTable::new(leaderlink.clone(), metrics.clone()))),
            ssh: Arc::new(Mutex::new(SshTable::new(leaderlink.clone(), metrics.clone()))),
            socks: Arc::new(Mutex::new(SocksTable::new(leaderlink, metrics.clone()))),
            uav: Arc::new(Mutex::new(UavTable::new(metrics)))
        }
    }

    pub fn run_jobs(&self) {
        loop {
            thread::sleep(Duration::from_secs(10));

            match self.dot11.lock() {
                Ok(dot11) => dot11.process_report(),
                Err(e) => error!("Could not acquire 802.11 table lock for report processing: {}", e)
            }
            
            match self.bluetooth.lock() {
                Ok(bluetooth) => {
                    bluetooth.calculate_metrics();
                    bluetooth.process_report();
                },
                Err(e) => error!("Could not acquire Bluetooth table lock for report processing: {}", e)
            }

            match self.tcp.lock() {
                Ok(tcp) => {
                    tcp.calculate_metrics();
                    tcp.process_report();
                },
                Err(e) => error!("Could not acquire TCP table lock for report processing: {}", e)
            }

            match self.udp.lock() {
                Ok(udp) => {
                    udp.calculate_metrics();
                    udp.process_report();
                },
                Err(e) => error!("Could not acquire UDP table lock for report processing: {}", e)
            }

            match self.dns.lock() {
                Ok(dns) => {
                    dns.calculate_metrics();
                    dns.process_report();
                },
                Err(e) => error!("Could not acquire DNS table lock for report processing: {}", e)
            }

            match self.ssh.lock() {
                Ok(ssh) => {
                    ssh.calculate_metrics();
                    ssh.process_report();
                },
                Err(e) => error!("Could not acquire SSH table lock for report processing: {}", e)
            }

            match self.socks.lock() {
                Ok(socks) => {
                    socks.calculate_metrics();
                    socks.process_report();
                },
                Err(e) => error!("Could not acquire SOCKS table lock for report processing: {}", e)
            }
        }
    }

}