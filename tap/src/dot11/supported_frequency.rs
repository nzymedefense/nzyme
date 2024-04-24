#[derive(Debug, Clone)]
pub struct SupportedFrequency {
    pub frequency: u32,
    pub bands: Vec<SupportedBand>
}

#[derive(Debug, Clone)]
pub enum SupportedBand {
    Mhz20,
    Mhz40Minus,
    Mhz40Plus,
    Mhz80,
    Mhz160,
    Mhz320
}