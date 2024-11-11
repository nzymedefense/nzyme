use strum_macros::Display;

#[derive(Debug, Clone)]
pub struct SupportedFrequency {
    pub frequency: u32,
    pub channel_widths: Vec<SupportedChannelWidth>
}

#[derive(Debug, Clone, Display)]
pub enum SupportedChannelWidth {
    Mhz20,
    Mhz40Minus,
    Mhz40Plus,
    Mhz80,
    Mhz160,
    Mhz320
}