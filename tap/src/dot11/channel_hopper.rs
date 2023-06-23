use std::{collections::HashMap, thread::{sleep, self}};

use anyhow::{Error, bail};
use log::{error, info, debug};
use rand::seq::SliceRandom;
use systemstat::Duration;

use super::nl::Nl;

pub struct ChannelHopper {
    pub device_assignments: HashMap<String, Vec<u32>>
}

impl ChannelHopper {

    pub fn new(device_names: Vec<String>) -> Result<Self, Error> {
        // Define adapters with their channels
        let mut adapters: HashMap<String, Vec<u32>> = HashMap::new();

        let nl = match Nl::new() {
            Ok(nl) => nl,
            Err(e) => {
                bail!("Could not establish Netlink connection: {}", e);
            }
        };

        for device_name in device_names {
            match nl.fetch_device(&device_name) {
                Ok(device) => { adapters.insert(device_name.clone(), device.supported_frequencies); },
                Err(e) => { error!("Could not fetch information of device [{}]. Not assigning to channels: {}", device_name, e); }
            }
        }

        let mut channels: HashMap<u32, Vec<String>> = HashMap::new();
        for (adapter, adapter_channels) in &adapters {
            for adapter_channel in adapter_channels {
                match channels.get_mut(&adapter_channel) {
                    Some(channel) => { channel.push(adapter.clone()); },
                    None => { channels.insert(*adapter_channel, vec![adapter.clone()]); }
                }
            }
        }

        let mut device_assignments: HashMap<String, Vec<u32>> = HashMap::new();

        for (channel, adapters) in channels {
            let adapter = Self::choose_weighted(&device_assignments, adapters);

            match device_assignments.get_mut(&adapter.clone()) {
                Some(channels) => { channels.push(channel) },
                None => { device_assignments.insert(adapter, vec![channel]); }
            }
        }

        Ok(ChannelHopper { device_assignments })
    }

    pub fn spawn_loop(&self) {
        let device_assigments = self.device_assignments.clone();

        info!("Channel map: {:?}", device_assigments);

        let nl = match Nl::new() {
            Ok(nl) => nl,
            Err(e) => {
                error!("Could not establish Netlink connection: {}", e);
                return
            }
        };

        thread::spawn(move || {
            let mut positions: HashMap<&String, u16> = HashMap::new();

            for (device, _) in &device_assigments {
                positions.insert(&device, 0);
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

                sleep(Duration::from_millis(500));
            }
        });
    }

    // This is dumb. Depends on order of calls. TODO build a proper balacing algorithm. (group by channel, split into even chunks?)
    fn choose_weighted(assignments: &HashMap<String, Vec<u32>>, eligible_adapters: Vec<String>) -> String {
        let mut channel_counts: HashMap<String, u16> = HashMap::new();
        for (assignment, channels) in assignments {
            match channel_counts.get_mut(assignment) {
                Some(count) => { *count += 1; },
                None => { channel_counts.insert(assignment.clone(), channels.len() as u16); }
            };
        }

        for eligible_adapter in eligible_adapters.clone() {
            let count = match channel_counts.get(&eligible_adapter) {
                Some(count) => *count,
                None => 0
            };

            if count == 0 {
                return eligible_adapter
            }
        }

        eligible_adapters.choose(&mut rand::thread_rng()).unwrap().clone()
    }

}