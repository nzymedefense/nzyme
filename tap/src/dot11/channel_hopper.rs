use std::{collections::HashMap, thread::{sleep, self}};

use anyhow::{Error, bail};
use log::{error, info, debug};
use systemstat::Duration;
use crate::configuration::ChannelWidthHoppingMode::limited;
use crate::configuration::WifiInterface;
use crate::dot11::supported_frequency::{SupportedChannelWidth, SupportedFrequency};
use crate::dot11::supported_frequency::SupportedChannelWidth::Mhz20;
use crate::helpers::network::{dot11_channel_to_frequency, Nl80211Band};

use super::nl::Nl;

const HOP_DWELL_MS: u64 = 250;

pub struct ChannelHopper {
    pub device_assignments: HashMap<String, Vec<SupportedFrequency>>
}

impl ChannelHopper {

    pub fn new(devices: HashMap<String, WifiInterface>) -> Result<Self, Error> {
        // Define adapters with their channels
        let mut adapters: HashMap<String, Vec<SupportedFrequency>> = HashMap::new();

        let mut nl = match Nl::new() {
            Ok(nl) => nl,
            Err(e) => {
                bail!("Could not establish Netlink connection: {}", e);
            }
        };

        for (device_name, device_configuration) in &devices {
            if !device_configuration.active {
                info!("Skipping disabled WiFi interface [{}].", device_name);
                continue;
            }

            if let Some(true) = device_configuration.disable_hopper {
                info!("Skipping hopper for WiFi interface [{}].", device_name);
                continue;
            }

            match nl.fetch_device_info(device_name) {
                Ok(device) => { adapters.insert(device_name.clone(), device.supported_frequencies); },
                Err(e) => { error!("Could not fetch information of device [{}]. Not assigning to channels: {}", device_name, e); }
            }
        }

        info!("Available adapters and channels: {:?}", adapters);

        // Map to config.
        let mut device_assignments: HashMap<String, Vec<SupportedFrequency>> = HashMap::new();
        for (device_name, device_configuration) in devices {
            if !device_configuration.active {
                debug!("Skipping disabled WiFi interface [{}].", device_name);
                continue;
            }

            if let Some(true) = device_configuration.disable_hopper {
                debug!("Skipping hopper for WiFi interface [{}].", device_name);
                continue;
            }

            // Check if selected frequencies are supported and bail if not.
            match adapters.get(&device_name) {
                None => bail!("WiFi adapter [{}] not found.", device_name),
                Some(adapter) => {
                    for channel in &*device_configuration.channels_2g {
                        let frequency: u16 = match dot11_channel_to_frequency(*channel, Nl80211Band::Band2GHz) {
                            Ok(frequency) => frequency,
                            Err(e) => bail!("Could not get frequency for 2G channel <{}> of device [{}]: {}",
                                channel, device_name, e)
                        };

                        if !Self::adapter_supports_frequency(adapter, frequency as u32) {
                            bail!("WiFi adapter [{}] does not support channel <{} / {} MHz>.",
                                device_name, channel, frequency);
                        }
                    }

                    for channel in &*device_configuration.channels_5g {
                        let frequency: u16 = match dot11_channel_to_frequency(*channel, Nl80211Band::Band5GHz) {
                            Ok(frequency) => frequency,
                            Err(e) => bail!("Could not get frequency for 5G channel <{}> of device [{}]: {}",
                                channel, device_name, e)
                        };

                        if !Self::adapter_supports_frequency(adapter, frequency as u32) {
                            bail!("WiFi adapter [{}] does not support channel <{} / {} MHz>.",
                                device_name, channel, frequency);
                        }
                    }

                    for channel in &*device_configuration.channels_6g {
                        let frequency: u16 = match dot11_channel_to_frequency(*channel, Nl80211Band::Band6GHz) {
                            Ok(frequency) => frequency,
                            Err(e) => bail!("Could not get frequency for 6G channel <{}> of device [{}]: {}",
                                channel, device_name, e)
                        };

                        if !Self::adapter_supports_frequency(adapter, frequency as u32) {
                            bail!("WiFi adapter [{}] does not support channel <{} / {} MHz>.",
                                device_name, channel, frequency);
                        }
                    }
                }
            }

            // Build final list of device assigments, including frequencies and channel widths.
            let mut frequencies: Vec<SupportedFrequency> = Vec::new();
            for channel in device_configuration.channels_2g {
                let (frequency, mut channel_widths) = match dot11_channel_to_frequency(channel, Nl80211Band::Band2GHz) {
                    Ok(frequency) => (frequency as u32, Self::get_all_supported_channel_widths(adapters.get(&device_name).unwrap(), frequency as u32)),
                    Err(e) => bail!("Could not get frequency for 2G channel <{}> of device [{}]: {}",
                                channel, device_name, e)
                };

                // Set channel widths to only 20Mhz for limited channel width hopping mode.
                if let Some(mode) = &device_configuration.channel_width_hopping_mode {
                    if mode.eq(&limited) {
                        channel_widths.clear();
                        channel_widths.push(Mhz20);
                    }
                }

                frequencies.push(SupportedFrequency { frequency, channel_widths })
            }
            for channel in device_configuration.channels_5g {
                let (frequency, mut channel_widths) = match dot11_channel_to_frequency(channel, Nl80211Band::Band5GHz) {
                    Ok(frequency) => (frequency as u32, Self::get_all_supported_channel_widths(adapters.get(&device_name).unwrap(), frequency as u32)),
                    Err(e) => bail!("Could not get frequency for 5G channel <{}> of device [{}]: {}",
                                channel, device_name, e)
                };

                // Set channel widths to only 20Mhz for limited channel width hopping mode.
                if let Some(mode) = &device_configuration.channel_width_hopping_mode {
                    if mode.eq(&limited) {
                        channel_widths.clear();
                        channel_widths.push(Mhz20);
                    }
                }

                frequencies.push(SupportedFrequency { frequency, channel_widths })
            }
            for channel in device_configuration.channels_6g {
                let (frequency, mut channel_widths) = match dot11_channel_to_frequency(channel, Nl80211Band::Band6GHz) {
                    Ok(frequency) => (frequency as u32, Self::get_all_supported_channel_widths(adapters.get(&device_name).unwrap(), frequency as u32)),
                    Err(e) => bail!("Could not get frequency for 6G channel <{}> of device [{}]: {}",
                                channel, device_name, e)
                };

                // Set channel widths to only 20Mhz for limited channel width hopping mode.
                if let Some(mode) = &device_configuration.channel_width_hopping_mode {
                    if mode.eq(&limited) {
                        channel_widths.clear();
                        channel_widths.push(Mhz20);
                    }
                }

                frequencies.push(SupportedFrequency { frequency, channel_widths })
            }

            device_assignments.insert(device_name, frequencies);
        }

        Ok(ChannelHopper { device_assignments })
    }

    pub fn spawn_loop(&self) {
        let device_assigments = self.device_assignments.clone();

        info!("Channel map: {:?}", device_assigments);

        let mut nl = match Nl::new() {
            Ok(nl) => nl,
            Err(e) => {
                error!("Could not establish Netlink connection: {}", e);
                return
            }
        };

        thread::spawn(move || {
            let mut positions: HashMap<&String, u16> = HashMap::new();

            for device in device_assigments.keys() {
                positions.insert(device, 0);
            }

            loop {
                for (device, channels) in &device_assigments {
                    let position = *positions.get(&device).unwrap() as usize;
                    let frequency_info = channels.get(position).unwrap();
                    let frequency = frequency_info.frequency;

                    for width in &*frequency_info.channel_widths {
                        match nl.set_device_frequency(device, frequency, width) { // EXTEND WITH WIDTH
                            Ok(()) => debug!("Device [{}] now tuned to frequency [{} Mhz / {:?}].", device, frequency, width),
                            Err(e) => error!("Could not tune [{}] to frequency [{} Mhz / {:?}]: {}", device, frequency, width, e)
                        }

                        sleep(Duration::from_millis(HOP_DWELL_MS));
                    }

                    let next_position = match position+1 == channels.len() {
                        true => 0, // Reached end of assigned channels. Next iteration starts at the beginning.
                        false => position+1
                    };

                    positions.insert(device, next_position as u16);
                }
            }
        });
    }

    pub fn get_device_assignments(&self) -> HashMap<String, Vec<SupportedFrequency>> {
        self.device_assignments.clone()
    }

    pub fn get_device_cycle_times(&self) -> HashMap<String, u64> {
        let mut cycle_times = HashMap::new();

        for (device, freqs) in &self.device_assignments {
            let mut hop_count: u64 = 0;

            for freq in freqs {
                hop_count += freq.channel_widths.len() as u64;
            }

            cycle_times.insert(device.clone(), hop_count* HOP_DWELL_MS);
        }

        cycle_times
    }

    fn adapter_supports_frequency(frequencies: &Vec<SupportedFrequency>, frequency: u32) -> bool {
        for f in frequencies {
            if f.frequency == frequency {
                return true
            }
        }

        false
    }

    fn get_all_supported_channel_widths(frequencies: &Vec<SupportedFrequency>, frequency: u32)
        -> Vec<SupportedChannelWidth> {
        for f in frequencies {
            if f.frequency == frequency {
                return f.clone().channel_widths
            }
        }

        vec![]
    }

}
