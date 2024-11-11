use std::sync::Arc;

use crate::wired::packets::{EthernetPacket, IPv6Packet};
use anyhow::Result;

// TODO remove linter hint after implementation
#[allow(clippy::unnecessary_wraps)]
pub fn parse(_packet: &Arc<EthernetPacket>) -> Result<IPv6Packet>  {
    Ok(IPv6Packet {})
}
