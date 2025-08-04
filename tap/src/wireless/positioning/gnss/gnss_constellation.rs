use strum_macros::Display;

#[derive(Debug, Display, Clone, Hash, Eq, PartialEq)]
pub enum GNSSConstellation {
    GNSS, GPS, GLONASS, BeiDou, Galileo
}