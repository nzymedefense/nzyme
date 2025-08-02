#[derive(Debug, Clone)]
pub enum GNSSConstellation {
    GNSS, GPS, GLONASS, BeiDou, Galileo
}

pub struct NotImplementedError;

impl TryFrom<&str> for GNSSConstellation {
    type Error = NotImplementedError;

    fn try_from(val: &str) -> Result<Self, Self::Error> {
        match val {
            "GNSS" => Ok(GNSSConstellation::GNSS),
            "GPS" => Ok(GNSSConstellation::GPS),
            "GLONASS" => Ok(GNSSConstellation::GLONASS),
            "BeiDou" => Ok(GNSSConstellation::BeiDou),
            "Galileo" => Ok(GNSSConstellation::Galileo),
            _ => Err(NotImplementedError)
        }
    }
}