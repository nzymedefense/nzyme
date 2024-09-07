use crate::context::context_data::{HostnameContextData, IpAddressContextData};

#[derive(Default, Debug)]
pub struct MacAddressContext {
    pub mac: String,
    pub ip_addresses: Vec<IpAddressContextData>,
    pub hostnames: Vec<HostnameContextData>,
}