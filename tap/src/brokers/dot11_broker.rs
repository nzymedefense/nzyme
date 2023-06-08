use std::{sync::Arc, thread};

use byteorder::{ByteOrder, LittleEndian};
use log::{info, debug};

use crate::{messagebus::bus::Bus, dot11::frames::Dot11Frame};

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

        if data.data.len() < header_length {
            debug!("Received WiFi frame shorter than reported header length.");
            return;
        }

        let radiotap_header = &data.data[0..header_length];

        info!("{:?}", radiotap_header);
    }

}