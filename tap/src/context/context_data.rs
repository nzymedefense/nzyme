use std::net::IpAddr;
use chrono::{DateTime, Utc};
use crate::context::context_source::ContextSource;

#[derive(Debug)]
pub struct IpAddressContextData {
    pub address: IpAddr,
    pub source: ContextSource,
    pub timestamp: DateTime<Utc>
}

#[derive(Debug)]
pub struct HostnameContextData {
    pub hostname: String,
    pub source: ContextSource,
    pub timestamp: DateTime<Utc>
}