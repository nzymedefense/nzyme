use std::{collections::HashMap, thread::{sleep, self}};

use anyhow::{Error, bail};
use log::{error, info, debug};
use systemstat::Duration;
use crate::configuration::WifiInterface;
use crate::helpers::network::{dot11_channel_to_frequency, Nl80211Band};

use super::nl::Nl;

pub struct ChannelHopper {
    pub device_assignments: HashMap<String, Vec<u32>>
}

impl ChannelHopper {

    pub fn new(devices: HashMap<String, WifiInterface>) -> Result<Self, Error> {
        // Define adapters with their channels
        let mut adapters: HashMap<String, Vec<u32>> = HashMap::new();

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

        let mut channels: HashMap<u32, Vec<String>> = HashMap::new();
        for (adapter, adapter_channels) in &adapters {
            for adapter_channel in adapter_channels {
                match channels.get_mut(adapter_channel) {
                    Some(channel) => { channel.push(adapter.clone()); },
                    None => { channels.insert(*adapter_channel, vec![adapter.clone()]); }
                }
            }
        }

        info!("Available adapters and channels: {:?}", adapters);

        // Map channels to config.
        let mut device_assignments: HashMap<String, Vec<u32>> = HashMap::new();
        for (device_name, device_configuration) in devices {
            if !device_configuration.active {
                info!("Skipping disabled WiFi interface [{}].", device_name);
                continue;
            }

            if let Some(true) = device_configuration.disable_hopper {
                info!("Skipping hopper for WiFi interface [{}].", device_name);
                continue;
            }

            match adapters.get(&device_name) {
                None => bail!("WiFi adapter [{}] not found.", device_name),
                Some(adapter) => {
                    for channel in &*device_configuration.channels_2g {
                        let frequency: u16 = match dot11_channel_to_frequency(*channel, Nl80211Band::Band2GHz) {
                            Ok(frequency) => frequency,
                            Err(e) => bail!("Could not get frequency for 2G channel <{}> of device [{}]: {}",
                                channel, device_name, e)
                        };

                        if !adapter.contains(&(frequency as u32)) {
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

                        if !adapter.contains(&(frequency as u32)) {
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

                        if !adapter.contains(&(frequency as u32)) {
                            bail!("WiFi adapter [{}] does not support channel <{} / {} MHz>.",
                                device_name, channel, frequency);
                        }
                    }
                }
            }

            let mut frequencies: Vec<u32> = Vec::new();
            for channel in device_configuration.channels_2g {
                frequencies.push(match dot11_channel_to_frequency(channel, Nl80211Band::Band2GHz) {
                    Ok(frequency) => frequency as u32,
                    Err(e) => bail!("Could not get frequency for 2G channel <{}> of device [{}]: {}",
                                channel, device_name, e)
                });
            }
            for channel in device_configuration.channels_5g {
                frequencies.push(match dot11_channel_to_frequency(channel, Nl80211Band::Band5GHz) {
                    Ok(frequency) => frequency as u32,
                    Err(e) => bail!("Could not get frequency for 5G channel <{}> of device [{}]: {}",
                                channel, device_name, e)
                });
            }
            for channel in device_configuration.channels_6g {
                frequencies.push(match dot11_channel_to_frequency(channel, Nl80211Band::Band6GHz) {
                    Ok(frequency) => frequency as u32,
                    Err(e) => bail!("Could not get frequency for 6G channel <{}> of device [{}]: {}",
                                channel, device_name, e)
                });
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
                    let frequency = channels.get(position).unwrap();

                    match nl.set_device_frequency(device, *frequency) {
                        Ok(()) => debug!("Device [{}] now tuned to frequency [{} Mhz].", device, frequency),
                        Err(e) => error!("Could not tune [{}] to frequency [{} Mhz]: {}", device, frequency, e)
                    }

                    let next_position = match position+1 == channels.len() {
                        true => 0, // Reached end of assigned channels. Next iteration starts at the beginning.
                        false => position+1
                    };

                    positions.insert(device, next_position as u16);
                }

                sleep(Duration::from_millis(1000));
            }
        });
    }

}
