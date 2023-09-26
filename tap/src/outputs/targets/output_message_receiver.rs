use std::sync::Arc;
use crate::dot11::frames::Dot11BeaconFrame;
use crate::ethernet::packets::DNSPacket;

pub trait OutputMessageReceiver {
    fn write_dns_packet(&self, dns: &Arc<DNSPacket>);
    fn write_dot11_beacon_frame(&self, frame: &Arc<Dot11BeaconFrame>);
}