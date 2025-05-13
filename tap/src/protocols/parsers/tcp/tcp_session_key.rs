use std::hash::{Hash, Hasher};
use std::net::IpAddr;

#[derive(Eq, Clone, Debug)]
pub struct TcpSessionKey {
    pub address_low: IpAddr,
    pub address_high: IpAddr,
    pub port_low: u16,
    pub port_high: u16,
}

impl TcpSessionKey {
    pub fn new(source_address: IpAddr,
               source_port: u16,
               destination_address:
               IpAddr, destination_port: u16) -> Self {
        if (source_address, source_port) <= (destination_address, destination_port) {
            TcpSessionKey {
                address_low: source_address,
                address_high: destination_address,
                port_low: source_port,
                port_high: destination_port,
            }
        } else {
            TcpSessionKey {
                address_low: destination_address,
                address_high: source_address,
                port_low: destination_port,
                port_high: source_port,
            }
        }
    }
}

impl PartialEq for TcpSessionKey {
    fn eq(&self, other: &Self) -> bool {
        self.address_high == other.address_high
            && self.address_low == other.address_low
            && self.port_low == other.port_low
            && self.port_high == other.port_high
    }
}

impl Hash for TcpSessionKey {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.address_low.hash(state);
        self.address_high.hash(state);
        self.port_low.hash(state);
        self.port_high.hash(state);
    }
}