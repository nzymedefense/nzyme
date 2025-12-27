use crate::wireless::dot11::sona::sona::SonaVersion;
use crate::wireless::dot11::supported_frequency::{SupportedChannelWidth, SupportedFrequency};

pub fn getSonaSupportedFrequencies(sona_version: SonaVersion) -> Vec<SupportedFrequency> {
    match sona_version {
        SonaVersion::One => vec![
            // 2.4 GHz.
            SupportedFrequency { frequency: 2412, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 2417, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 2422, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 2427, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 2432, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 2437, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 2442, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 2447, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 2452, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 2457, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 2462, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 2467, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 2472, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 2484, channel_widths: vec![SupportedChannelWidth::Mhz20] },

            // 5 GHz.
            SupportedFrequency { frequency: 5180, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5200, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5220, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5240, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5260, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5280, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5300, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5320, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5500, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5520, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5540, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5560, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5580, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5600, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5620, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5640, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5660, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5680, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5700, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5720, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5745, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5765, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5785, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5805, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5825, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5825, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5865, channel_widths: vec![SupportedChannelWidth::Mhz20] },
            SupportedFrequency { frequency: 5885, channel_widths: vec![SupportedChannelWidth::Mhz20] },
        ]
    }
}