use strum_macros::Display;

#[derive(Debug, Display, Eq, PartialEq)]
pub enum NMEASentenceType {
    GPGGA,
    GPGSA,
    GPGSV
}

pub struct NotImplementedError;

impl TryFrom<&str> for NMEASentenceType {
    type Error = NotImplementedError;

    fn try_from(val: &str) -> Result<Self, Self::Error> {
        match val {
            "GPGGA" => Ok(NMEASentenceType::GPGGA),
            "GPGSA" => Ok(NMEASentenceType::GPGSA),
            "GPGSV" => Ok(NMEASentenceType::GPGSV),
            _ => Err(NotImplementedError)
        }
    }
}