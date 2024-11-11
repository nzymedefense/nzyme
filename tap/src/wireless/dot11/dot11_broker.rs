use std::{panic::catch_unwind, sync::Arc, thread};

use anyhow::{bail, Error};
use bitvec::{order::Lsb0, view::BitView};
use byteorder::{ByteOrder, LittleEndian};
use log::{error, info, trace, warn};

use crate::{messagebus::bus::Bus, to_pipeline};
use crate::helpers::network::dot11_frequency_to_channel;
use crate::messagebus::channel_names::Dot11ChannelName;
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
        // Parse header.
        if data.data.len() < 4 {
            trace!("Received WiFi frame is too short. [{:?}]", data);
            return;
        }

        let header_length = LittleEndian::read_u16(&data.data[2..4]) as usize;

        if data.data.len() < header_length {
            trace!("Received WiFi frame shorter than reported header length.");
            return;
        }

        // Parse Radiotap header.
        let header_data = &data.data[0..header_length];

        if data.data.len() < 8 {
            trace!("Received WiFi frame too short to fit present flags and anything else.");
            return;
        }

        // Present flags bitmask.
        let (ca, present_flags) = match Self::parse_present_flags(&header_data[4..header_data.len()]) {
            Ok((cursor_advance, flags)) => (cursor_advance, flags),
            Err(e) => {
                warn!("Could not parse radiotap present flag bitmask: {}", e);
                return
            }
        };

        let mut cursor = 4+ca;

        // Variable fields follow. Not always set.

        // TSFT.
        if present_flags.tsft {
            // not parsing
            cursor += 8;
        }

        // Flags.
        let flags = if present_flags.flags {
            let frame_flags = Self::parse_flags(&header_data[cursor]);

            // Abort if checksum check failed. (Driver should not have passed this along.)
            if frame_flags.bad_fcs {
                trace!("Filtering out bad FCS frame from interface [{}].", data.interface_name);
                return
            }

            cursor += 1;

            Some(frame_flags)
        } else {
            cursor += 1;
            None
        };
        
        // Data Rate.
        let data_rate: Option<u16> = if present_flags.rate {
            if header_data.len() <= cursor {
                return
            }

            let data_rate = (header_data[cursor] as u16)*500;
            cursor += 1;

            Some(data_rate)
        } else {
            cursor += 1;
            None
        };

        // Channel.
        let frequency = if present_flags.channel {
            let frequency = LittleEndian::read_u16(&data.data[cursor..cursor+2]);
            
            if frequency == 0 {
                trace!("Filtering out frame with bad reported frequency on [{}].",
                    data.interface_name);
                return
            }
            
            cursor += 2;

            // Skip the channel flags. Not parsing.
            cursor += 2;

            Some(frequency)
        } else {
            cursor += 4;
            None
        };

        // FHSS.
        if present_flags.fhss {
            // not parsing
            cursor += 2;
        }

        // Antenna signal (dBm)
        let antenna_signal = if present_flags.antenna_signal_dbm {
            if header_data.len() <= cursor {
                return
            }

            let dbm = header_data[cursor] as i8;
            cursor += 1;

            Some(dbm)
        } else {
            cursor += 1;
            None
        };

        // Antenna noise (dBm)
        if present_flags.antenna_noise_dbm {
            // not parsing
            cursor += 1;
        }

        // Lock quality.
        if present_flags.lock_quality {
            // not parsing
            cursor += 2;
        }

        // TX attenuation.
        if present_flags.tx_attenuation {
            // not parsing
            cursor += 1;
        }

        // TX attenuation (dB)
        if present_flags.tx_attenuation_db {
            // not parsing
            cursor += 2;
        }

        // TX power (dBm)
        if present_flags.tx_power_dbm {
            // not parsing
            cursor += 1;
        }

        // Antenna.
        let antenna = if present_flags.antenna {
            if header_data.len() <= cursor {
                return
            }
            
            let antenna = header_data[cursor];
            cursor += 1;

            Some(antenna)
        } else {
            cursor += 1;
            None
        };

        // There are more variable fields after this, which we are not currently parsing.
        let is_wep = flags.map(|flags| flags.wep);
        
        let channel = if let Some(frequency) = frequency {
            let channel = match dot11_frequency_to_channel(frequency as u32) {
                Ok(c) => c.channel,

                Err(e) => {
                    warn!("Could not parse channel number from frequency: {}. Present Flags: {:?}", e, present_flags);
                    return
                }
            };

            Some(channel)
        } else {
            None
        };

        let radiotap_header = RadiotapHeader { 
            is_wep,
            data_rate,
            frequency,
            channel,
            antenna_signal,
            antenna
        };

        let payload = &data.data[header_length..data.data.len()];

        if payload.len() < 2 {
            trace!("Payload too short.");
            return;
        }

        let frame_type = parse_frame_type(&payload[0]);

        // Send to processor pipeline.
        to_pipeline!(
            Dot11ChannelName::Dot11FramesPipeline,
            bus.dot11_frames_pipeline.sender,
            Arc::new(Dot11Frame {
                header: radiotap_header,
                frame_type: frame_type.frame_subtype,
                payload: payload.to_vec(),
                length: data.data.len()
            }),
            payload.len() as u32
        );
    }

    fn parse_present_flags(mask: &[u8]) -> Result<(usize, RadiotapHeaderPresentFlags), Error> {
        assert!(mask.len() >= 4);

        let mut flags_cursor = 4;
        let bmask = mask[0..flags_cursor].view_bits::<Lsb0>();

        let first_flags = RadiotapHeaderPresentFlags {
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
        };

        if first_flags.ext {
            loop {
                /*
                 * Some WiFi adapters will have multiple sets of present flags, indicated by
                 * the `ext` set to true. It is currently unclear why. Best way here appears to be
                 * using the first set of flags, ignore any that follows, but sufficiently advance
                 * the cursor to resume reading of trailing information in the frame.
                 */

                if mask.len() < flags_cursor+4 {
                    bail!("Invalid flags length {}, cursor: {}", mask.len(), flags_cursor);
                }

                let next_mask = mask[flags_cursor..flags_cursor+4].view_bits::<Lsb0>();
                flags_cursor += 4;

                if !*next_mask.get(29).unwrap() {
                    break;
                }
            }
        }

        Ok((flags_cursor, first_flags))
    }

    fn parse_flags(mask: &u8) -> RadiotapHeaderFlags {
        let bmask = mask.view_bits::<Lsb0>();
        
        /*
         * We are using unwrap() here because the passed u8
         * ensures enough bits for the access ops.
         */

        RadiotapHeaderFlags { 
            cfp: *bmask.get(0).unwrap(), 
            preamble: *bmask.get(1).unwrap(),
            wep: *bmask.get(2).unwrap(),
            fragmentation: *bmask.get(3).unwrap(),
            fcs_at_end: *bmask.get(4).unwrap(),
            data_pad: *bmask.get(5).unwrap(),
            bad_fcs: *bmask.get(6).unwrap(),
            short_gi: *bmask.get(7).unwrap(),
        }
    }

}

fn parse_frame_type(mask: &u8) -> FrameTypeInformation {
    match mask {
        // Management.
        0b0000_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::AssociationRequest },
        0b0001_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::AssociationResponse },
        0b0010_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::ReAssociationRequest },
        0b0011_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::ReAssociationResponse },
        0b0100_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::ProbeRequest },
        0b0101_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::ProbeResponse },
        0b0110_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::TimingAdvertisement },
        0b0111_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::Reserved },
        0b1000_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::Beacon },
        0b1001_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::Atim },
        0b1010_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::Disassocation },
        0b1011_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::Authentication },
        0b1100_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::Deauthentication },
        0b1101_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::Action },
        0b1110_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::ActionNoAck },
        0b1111_0000 => FrameTypeInformation { frame_type: FrameType::Management, frame_subtype: FrameSubType::Reserved },

        // Control.
        0b0000_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::Reserved },
        0b0001_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::Reserved },
        0b0010_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::Trigger },
        0b0011_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::Tack },
        0b0100_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::BeamformingReportPoll },
        0b0101_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::VhtHeNdpAnnouncement },
        0b0110_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::ControlFrameExtension },
        0b0111_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::ControlWrapper },
        0b1000_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::BlockAckRequest },
        0b1001_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::BlockAck },
        0b1010_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::PsPoll },
        0b1011_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::Rts },
        0b1100_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::Cts },
        0b1101_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::Ack },
        0b1110_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::CfEnd },
        0b1111_0100 => FrameTypeInformation { frame_type: FrameType::Control, frame_subtype: FrameSubType::CfEndCfAck },

        // Data.
        0b0000_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::Data },
        0b0001_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::Reserved },
        0b0010_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::Reserved },
        0b0011_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::Reserved },
        0b0100_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::Null },
        0b0101_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::Reserved },
        0b0110_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::Reserved },
        0b0111_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::Reserved },
        0b1000_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::QosData },
        0b1001_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::QosDataCfAck },
        0b1010_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::QosDataCfPoll },
        0b1011_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::QosDataCfAckCfPoll },
        0b1100_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::QosNull },
        0b1101_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::Reserved },
        0b1110_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::QosCfPoll },
        0b1111_1000 => FrameTypeInformation { frame_type: FrameType::Data, frame_subtype: FrameSubType::QosCfAckCfPoll },

        // Extension.
        0b0000_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::DmgBeacon },
        0b0001_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::S1gBeacon },
        0b0010_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::Reserved },
        0b0011_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::Reserved },
        0b0100_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::Reserved },
        0b0101_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::Reserved },
        0b0110_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::Reserved },
        0b0111_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::Reserved },
        0b1000_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::Reserved },
        0b1001_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::Reserved },
        0b1010_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::Reserved },
        0b1011_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::Reserved },
        0b1100_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::Reserved },
        0b1101_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::Reserved },
        0b1110_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::Reserved },
        0b1111_1100 => FrameTypeInformation { frame_type: FrameType::Extension, frame_subtype: FrameSubType::Reserved },

        _ => FrameTypeInformation { frame_type: FrameType::Invalid, frame_subtype: FrameSubType::Invalid }
    }
}