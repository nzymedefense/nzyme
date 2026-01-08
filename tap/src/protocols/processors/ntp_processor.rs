use std::sync::{Arc, Mutex};
use log::error;
use crate::state::tables::ntp_table::NtpTable;
use crate::wired::packets::NtpPacket;

pub struct NtpProcessor {
    ntp_table: Arc<Mutex<NtpTable>>
}

impl NtpProcessor {

    pub fn new(ntp_table: Arc<Mutex<NtpTable>>) -> Self {
        Self { ntp_table }
    }

    pub fn process(&mut self, packet: Arc<NtpPacket>) {
        match self.ntp_table.lock() {
            Ok(mut ntp_table) => {
                ntp_table.register_ntp_packet(packet);
            },
            Err(e) => {
                error!("Could not acquire NTP table: {}", e);
            }
        }
    }
}