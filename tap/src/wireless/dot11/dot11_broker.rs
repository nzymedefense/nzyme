use std::{panic::catch_unwind, sync::Arc, thread};

use anyhow::{bail, Error};
use bitvec::{order::Lsb0, view::BitView};
use byteorder::{ByteOrder, LittleEndian};
use log::{error, info, trace, warn};

use crate::{messagebus::bus::Bus, to_pipeline};
use crate::helpers::network::dot11_frequency_to_channel;
use crate::messagebus::channel_names::Dot11ChannelName;
use crate::protocols::parsers::dot11::dot11_header_parser;
use crate::wireless::dot11::frames::{Dot11Frame, Dot11RawFrame, FrameSubType, FrameType, FrameTypeInformation, RadiotapHeader, RadiotapHeaderFlags, RadiotapHeaderPresentFlags};

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
                for frame in receiver.iter() {
                    let handler_result = catch_unwind(|| {
                        Self::handle(&frame, &bus)
                    });

                    if handler_result.is_err() {
                        error!("Unexpected error in frame handling. Skipping.");
                    }
                }
            });
        }
    }

    #[allow(unused_assignments)] // for the last cursor assignment, which is good to avoid bugs when extending.
    fn handle(data: &Arc<Dot11RawFrame>, bus: &Arc<Bus>) {
        let (dot11_frame, payload_len) = match dot11_header_parser::parse(data) {
            Ok((dot11_header, payload_len)) => (dot11_header, payload_len),
            Err(e) => {
                trace!("{}", e);
                return;
            }
        };

        // Send to processor pipeline.
        to_pipeline!(
            Dot11ChannelName::Dot11FramesPipeline,
            bus.dot11_frames_pipeline.sender,
            Arc::new(dot11_frame),
            payload_len
        );
    }
}

