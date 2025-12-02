#[derive(Debug, Clone, Copy)]
pub enum SentenceType {
    Nmea,
    UbxMonRf,
    Unknown(u8),
}

impl SentenceType {
    pub fn from_u8(v: u8) -> Self {
        match v {
            0 => SentenceType::Nmea,
            1 => SentenceType::UbxMonRf,
            other => SentenceType::Unknown(other),
        }
    }
}