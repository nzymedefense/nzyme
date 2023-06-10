use std::{sync::Arc, thread};

use anyhow::{Error, bail};
use bitvec::{view::BitView, order::Lsb0};
use byteorder::{ByteOrder, LittleEndian};
use log::{info, debug, warn, trace};

use crate::{messagebus::bus::Bus, dot11::frames::{Dot11Frame, RadiotapHeader, RadiotapHeaderPresentFlags, RadiotapHeaderFlags}};

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

    #[allow(unused_assignments)] // for the last cursor assignment, which is good to avoid bugs when extending.
    fn handle(data: &Arc<Dot11Frame>, _bus: &Arc<Bus>) {
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

        // Parse Radiotap header.
        let header_data = &data.data[0..header_length];

        if data.data.len() < 8 {
            debug!("Received WiFi frame too short to fit present flags and anything else.");
            return;
        }

        // Present flags bitmask.
        let present_flags = match Self::parse_present_flags(&header_data[4..8]) {
            Ok(flags) => flags,
            Err(e) => {
                warn!("Could not parse radiotap present flag bitmask: {}", e);
                return
            }
        };

        let mut cursor = 8;

        // Variable fields. Not always set.

        // TSFT.
        if present_flags.tsft {
            // not parsing
            cursor += 8;
        }

        // Flags.
        let flags = if present_flags.flags {
            let frame_flags = Self::parse_flags(&header_data[cursor]);

            // Abort if checksum check failed.
            if frame_flags.bad_fcs {
                // TODO metrics here.
                trace!("Filtering out bad FCS frame from interface [{}].", data.interface_name);
                return
            }

            cursor += 1;

            Option::Some(frame_flags)
        } else {
            cursor += 1;
            Option::None
        };
        
        // Data Rate.
        let data_rate: Option<u16> = if present_flags.rate {
            if header_data.len() <= cursor {
                return
            }

            let data_rate = ((header_data[cursor] as u16)*500) as u16;
            cursor += 1;

            Option::Some(data_rate)
        } else {
            cursor += 1;
            Option::None
        };

        // Channel.
        let frequency = if present_flags.channel {
            let frequency = LittleEndian::read_u16(&data.data[cursor..cursor+2]);
            cursor += 2;

            // Skip the channel flags. Not parsing.
            cursor += 2;

            Option::Some(frequency)
        } else {
            cursor += 4;
            Option::None
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

            Option::Some(dbm)
        } else {
            cursor += 1;
            Option::None
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

            Option::Some(antenna)
        } else {
            cursor += 1;
            Option::None
        };

        // There are more variable fields after this, which we are not currently parsing.

        let is_wep = if flags.is_some() {
            Option::Some(flags.unwrap().wep)
        } else {
            Option::None
        };
        
        let channel = if frequency.is_some() {
            let channel = match Self::frequency_to_channel(frequency.unwrap()) {
                Ok(c) => c,
                Err(e) => {
                    warn!("Could not parse channel number from frequency: {}. Present Flags: {:?}", e, present_flags);
                    return
                }
            };

            Option::Some(channel)
        } else {
            Option::None
        };

        let radiotap = RadiotapHeader { 
            is_wep,
            data_rate,
            frequency,
            channel,
            antenna_signal,
            antenna
        };

        info!("{:?}", radiotap);
    }

    fn parse_present_flags(mask: &[u8]) -> Result<RadiotapHeaderPresentFlags, Error> {
        if mask.len() != 4 {
            bail!("Radiotap present flags must be 4 bytes, provided <{}>.", mask.len())
        }

        let bmask = mask.view_bits::<Lsb0>();

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
            ext: *bmask.get(29).unwrap(),
        })
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

    fn frequency_to_channel(f: u16) -> Result<u16, Error> {
        match f {
            2412 => Ok(1),
            2417 => Ok(2),
            2422 => Ok(3),
            2427 => Ok(4),
            2432 => Ok(5),
            2437 => Ok(6),
            2442 => Ok(7),
            2447 => Ok(8),
            2452 => Ok(9),
            2457 => Ok(10),
            2462 => Ok(11),
            2467 => Ok(12),
            2472 => Ok(13),
            2484 => Ok(14),
            
            5160 => Ok(32),
            5180 => Ok(36),
            5190 => Ok(38),
            5200 => Ok(40),
            5210 => Ok(42),
            5220 => Ok(44),
            5230 => Ok(46),
            5240 => Ok(48),
            5250 => Ok(50),
            5260 => Ok(52),
            5270 => Ok(54),
            5280 => Ok(56),
            5290 => Ok(58),
            5300 => Ok(60),
            5310 => Ok(62),
            5320 => Ok(64),
            5340 => Ok(68),
            5480 => Ok(96),
            5500 => Ok(100),
            5510 => Ok(102),
            5520 => Ok(104),
            5530 => Ok(106),
            5540 => Ok(108),
            5550 => Ok(110),
            5560 => Ok(112),
            5570 => Ok(114),
            5580 => Ok(116),
            5590 => Ok(118),
            5600 => Ok(120),
            5610 => Ok(122),
            5620 => Ok(124),
            5630 => Ok(126),
            5640 => Ok(128),
            5660 => Ok(132),
            5670 => Ok(134),
            5680 => Ok(136),
            5690 => Ok(138),
            5700 => Ok(140),
            5710 => Ok(142),
            5720 => Ok(144),
            5745 => Ok(149),
            5755 => Ok(151),
            5765 => Ok(153),
            5775 => Ok(155),
            5785 => Ok(157),
            5795 => Ok(159),
            5805 => Ok(161),
            5815 => Ok(163),
            5825 => Ok(165),
            5835 => Ok(167),
            5845 => Ok(169),
            5855 => Ok(171),
            5865 => Ok(173),
            5875 => Ok(175),
            5885 => Ok(177),
            _ => bail!("Unknown channel for frequency <{}>", f)
        }
    }

}