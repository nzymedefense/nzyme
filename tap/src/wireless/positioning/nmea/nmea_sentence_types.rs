use strum_macros::Display;

#[derive(Debug, Display, Eq, PartialEq)]
pub enum NMEASentenceType {
    GGA,
    GSA,
    GSV
}

pub struct NotImplementedError;

impl TryFrom<&str> for NMEASentenceType {
    type Error = NotImplementedError;

    fn try_from(val: &str) -> Result<Self, Self::Error> {
        match val {
            "GGA" => Ok(NMEASentenceType::GGA),
            "GSA" => Ok(NMEASentenceType::GSA),
            "GSV" => Ok(NMEASentenceType::GSV),
            _ => Err(NotImplementedError)
        }
    }
}