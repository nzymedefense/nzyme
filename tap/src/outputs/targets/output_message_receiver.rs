use std::sync::Arc;
use crate::dot11::frames::Dot11BeaconFrame;
use crate::ethernet::packets::DNSPacket;

pub trait OutputMessageReceiver {
    fn write_dns_packets(&self, packet: &Vec<Arc<DNSPacket>>);
    fn write_dot11_beacon_frames(&self, frames: &Vec<Arc<Dot11BeaconFrame>>);
}