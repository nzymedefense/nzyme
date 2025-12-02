use strum_macros::Display;

#[derive(Debug, Display, Clone, Hash, Eq, PartialEq)]
pub enum GNSSConstellation {
    GPS, GLONASS, BeiDou, Galileo
}

impl GNSSConstellation {
    pub fn from_axia_u8(v: u8) -> Self {
        match v {
            0 => GNSSConstellation::GPS,
            1 => GNSSConstellation::Galileo,
            2 => GNSSConstellation::GLONASS,
            4 => GNSSConstellation::BeiDou,
            _ => panic!("Invalid GNSS constellation value: {}", v)
        }
    }

    pub fn as_str(&self) -> &'static str {
        match self {
            GNSSConstellation::GPS => "GPS",
            GNSSConstellation::Galileo => "Galileo",
            GNSSConstellation::GLONASS => "GLONASS",
            GNSSConstellation::BeiDou => "BeiDou",
            _ => panic!("Invalid GNSS constellation value.")
        }
    }
}