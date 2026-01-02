use crate::wireless::positioning::gnss_constellation::GNSSConstellation;

/*
 * UBX messages identify satellites using canonical GNSS-specific identifiers
 * (gnssId + svId). The svId is the natural satellite number within a constellation.
 *
 * GLONASS is special because its satellites are identified by *frequency slot
 * numbers* rather than globally unique PRNs. UBX therefore reports GLONASS
 * satellites by slot number (svId = 1..24).
 *
 * NMEA, however, requires a single numeric "satellite ID" space across all
 * constellations. To avoid collisions with GPS PRNs (1..32) and SBAS (33..64),
 * NMEA represents GLONASS satellites as: NMEA_ID = 64 + GLONASS_slot
 *
 * This function converts a UBX GLONASS svId (slot number) into the corresponding
 * NMEA satellite ID so that UBX-derived measurements can be joined with NMEA GSV
 * data using a consistent, presentation-oriented numbering scheme.
 */
pub fn ubx_to_nmea_prn(constellation: &GNSSConstellation, sv_id: u8) -> Option<u8> {
    // Common “invalid/not used” to discard.
    if sv_id == 0 || sv_id == 255 {
        return None;
    }

    match constellation {
        GNSSConstellation::GLONASS => (1..=32).contains(&sv_id).then(|| sv_id + 64),
        _ => Some(sv_id),
    }
}
