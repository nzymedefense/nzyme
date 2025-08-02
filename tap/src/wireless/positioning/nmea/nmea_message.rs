use crate::wireless::positioning::gnss::gnss_constellation::GNSSConstellation;

#[derive(Debug)]
pub struct NMEAMessage {
    pub interface: String,
    pub constellation: GNSSConstellation,
    pub sentence: String
}