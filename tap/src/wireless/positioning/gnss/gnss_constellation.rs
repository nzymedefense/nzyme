use strum_macros::Display;

#[derive(Debug, Display, Clone, Hash, Eq, PartialEq)]
pub enum GNSSConstellation {
    GPS, GLONASS, BeiDou, Galileo
}