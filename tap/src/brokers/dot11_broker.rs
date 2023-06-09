use std::{sync::Arc, thread};

use anyhow::{Error, bail};
use bitvec::{view::BitView, order::Lsb0};
use byteorder::{ByteOrder, LittleEndian};
use log::{info, debug, warn};

use crate::{messagebus::bus::Bus, dot11::frames::{Dot11Frame, RadiotapHeader, RadiotapHeaderPresentFlags}};

pub struct Dot11Broker {
    num_threads: usize,
    bus: Arc<Bus>
}


impl Dot11Broker {

    pub fn new(bus: Arc<Bus>, num_threads: usize) -> Self {
        Self {
            num_threads,
            bus
        }
    }

    pub fn run(&mut self) {
        for num in 0..self.num_threads {
            info!("Installing WiFi broker thread <{}>.", num);
           
            let receiver = self.bus.dot11_broker.receiver.clone();
            let bus = self.bus.clone();
            thread::spawn(move || {
                for packet in receiver.iter() {
                    Self::handle(&packet, &bus);
                }
            });
        }
    }

    fn handle(data: &Arc<Dot11Frame>, bus: &Arc<Bus>) {
        // Parse header.
        if data.data.len() < 4 {
            debug!("Received WiFi frame is too short. [{:?}]", data);
            return;
        }

        let header_length = LittleEndian::read_u16(&data.data[2..4]) as usize;

        if (header_length < 14) {
            debug!("Received WiFi frame header is too short.");
            return;
        }

        if data.data.len() < header_length {
            debug!("Received WiFi frame shorter than reported header length.");
            return;
        }

        // Parse Radiotap header.
        let header_data = &data.data[0..header_length];

        // Present flags bitmask.
        let present_flags = match Self::parse_present_flags(&header_data[4..8]) {
            Ok(flags) => flags,
            Err(e) => {
                warn!("Could not parse radiotap present flag bitmask: {}", e);
                return
            }
        };
        
        // Data Rate.
        let data_rate: Option<u16> = if present_flags.rate {
            Option::Some(((header_data[9] as u16)*500) as u16)
        } else {
            Option::None
        };

        let radiotap = RadiotapHeader { 
            data_rate
        };

        info!("{:?}", radiotap);
    }

    fn parse_present_flags(word: &[u8]) -> Result<RadiotapHeaderPresentFlags, Error> {
        if word.len() != 4 {
            bail!("Radiotap present flags must be 4 bytes, provided <{}>.", word.len())
        }

        let bmask = word.view_bits::<Lsb0>();

        /*
         * We are using unwrap() here because the bounds length 
         * above ensures enough bits for the access ops.
         */

        Ok(RadiotapHeaderPresentFlags { 
            tsft: *bmask.get(0).unwrap(),
            flags: *bmask.get(1).unwrap(),
            rate: *bmask.get(2).unwrap(),
            channel: *bmask.get(3).unwrap(),
            fhss: *bmask.get(4).unwrap(),
            antenna_signal_dbm: *bmask.get(5).unwrap(),
            antenna_noise_dbm: *bmask.get(6).unwrap(),
            lock_quality: *bmask.get(7).unwrap(),
            tx_attenuation: *bmask.get(8).unwrap(),
            tx_attenuation_db: *bmask.get(9).unwrap(),
            tx_power_dbm: *bmask.get(10).unwrap(),
            antenna: *bmask.get(11).unwrap(),
            antenna_signal_db: *bmask.get(12).unwrap(),
            antenna_noise_db: *bmask.get(13).unwrap(),
            rx_flags: *bmask.get(14).unwrap(),
            tx_flags: *bmask.get(15).unwrap(),
            data_retries: *bmask.get(16).unwrap(),
            channel_plus: *bmask.get(17).unwrap(),
            mcs: *bmask.get(18).unwrap(),
            ampdu: *bmask.get(19).unwrap(),
            vht: *bmask.get(20).unwrap(),
            timestamp: *bmask.get(21).unwrap(),
            he_info: *bmask.get(22).unwrap(),
            hemu_info: *bmask.get(23).unwrap(),
            zero_length_psdu: *bmask.get(24).unwrap(),
            lsig: *bmask.get(25).unwrap(),
            tlvs: *bmask.get(26).unwrap(),
            radiotap_ns_next: *bmask.get(27).unwrap(),
            vendor_nx_next: *bmask.get(28).unwrap(),
            ext: *bmask.get(29).unwrap()
        })
    }

}