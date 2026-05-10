use std::sync::Arc;
use anyhow::{bail, Error};
use crate::helpers::network::{dot11_channel_to_frequency, Nl80211Band};
use crate::wireless::dot11::engagement::engagement_interface::EngagementInterface;

pub fn extract_frequencies_from_interface(interface: &Arc<EngagementInterface>)
    -> Result<Vec<u32>, Error> {

    let mut frequencies: Vec<u32> = Vec::new();

    for channel in &interface.supported_channels_2g {
        let frequency: u16 = match dot11_channel_to_frequency(*channel, Nl80211Band::Band2GHz) {
            Ok(frequency) => frequency,
            Err(e) => bail!("Could not get frequency for 2G channel <{}> of device [{}]: {}",
                channel, interface.name, e)
        };

        frequencies.push(frequency as u32);
    }

    for channel in &interface.supported_channels_5g {
        let frequency: u16 = match dot11_channel_to_frequency(*channel, Nl80211Band::Band5GHz) {
            Ok(frequency) => frequency,
            Err(e) => bail!("Could not get frequency for 5G channel <{}> of device [{}]: {}",
                channel, interface.name, e)
        };

        frequencies.push(frequency as u32);
    }

    for channel in &interface.supported_channels_6g {
        let frequency: u16 = match dot11_channel_to_frequency(*channel, Nl80211Band::Band6GHz) {
            Ok(frequency) => frequency,
            Err(e) => bail!("Could not get frequency for 6G channel <{}> of device [{}]: {}",
                channel, interface.name, e)
        };

        frequencies.push(frequency as u32);
    }

    Ok(frequencies)
}