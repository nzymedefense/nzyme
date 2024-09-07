use std::net::{IpAddr, Ipv4Addr};
use pnet::datalink;
use pnet::datalink::{MacAddr, NetworkInterface};

pub fn get_first_ipv4_address_of_interface(interface_name: &str) -> Option<Ipv4Addr> {
    get_interface(interface_name)
        .and_then(|interface| {
            interface.ips
                .iter()
                .find_map(|ip_network| match ip_network.ip() {
                    IpAddr::V4(ipv4) => Some(ipv4),
                    _ => None,  // Ignore Ipv6 addresses
                })
        })
}

pub fn get_mac_address_of_interface(interface_name: &str) -> Option<MacAddr> {
    get_interface(interface_name)
        .and_then(|interface| interface.mac)
}

fn get_interface(name: &str) -> Option<NetworkInterface> {
    datalink::interfaces()
        .into_iter()
        .find(|interface| interface.name == name)
}