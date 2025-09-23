use strum_macros::Display;

#[derive(Debug, Clone)]
pub struct SupportedFrequency {
    pub frequency: u32,
    pub channel_widths: Vec<SupportedChannelWidth>
}

#[derive(Debug, Clone, Display, Default)]
pub enum SupportedChannelWidth {
    #[default]
    Mhz20,
    Mhz40Minus,
    Mhz40Plus,
    Mhz80,
    Mhz160,
    Mhz320
}