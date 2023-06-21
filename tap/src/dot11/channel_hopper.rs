use std::collections::HashMap;

use log::{error, info};
use rand::seq::SliceRandom;

use super::nl::Nl;

pub struct ChannelHopper {
    device_names: Vec<String>
}

impl ChannelHopper {

    pub fn new(device_names: Vec<String>) -> Self {
        ChannelHopper { device_names }
    }

    pub fn initialize(&self) {
         // Define adapters with their channels
        let mut adapters: HashMap<String, Vec<u32>> = HashMap::new();

        let nl = Nl{};

        for device_name in &self.device_names {
            match nl.fetch_device(device_name) {
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

        let mut assignments: HashMap<String, Vec<u32>> = HashMap::new();

        for (channel, adapters) in channels {
            let adapter = self.choose_weighted(&assignments, adapters);

            match assignments.get_mut(&adapter.clone()) {
                Some(channels) => { channels.push(channel) },
                None => { assignments.insert(adapter, vec![channel]); }
            }
        }

        info!("ASSIGNMENTS: {:?}", assignments);
    }

    // This is dumb. Depends on order of calls. TODO build a proper balacing algorithm. (group by channel, split into even chunks?)
    fn choose_weighted(&self, assignments: &HashMap<String, Vec<u32>>, eligible_adapters: Vec<String>) -> String {
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