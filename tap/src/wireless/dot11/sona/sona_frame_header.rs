/*
 * Per-frame metadata header sent by Sona ahead of the raw 802.11 bytes.
 *
 * Wire layout (10 bytes, little-endian):
 *   [0..4]   capture_uptime_ms (u32)
 *   [4..6]   freq_mhz          (u16)
 *   [6]      rssi_dbm          (i8)
 *   [7]      reserved
 *   [8]      rate_flags        (u8)
 *   [9]      rate_code         (u8)
 *
 * The reserved byte exists so freq_mhz is 2-byte aligned and the 802.11
 * body that follows is 2-byte aligned. Don't repurpose it without bumping
 * WIRE_PROTOCOL_VERSION on both sides.
 */

#[derive(Debug, Clone)]
pub struct SonaFrameHeader {
    pub capture_uptime_ms: u32,
    pub freq_mhz: u16,
    pub rssi_dbm: Option<f32>,
    pub rate_flags: u8,
    pub rate_code: u8,
}

impl SonaFrameHeader {
    pub const BYTES: usize = 10;

    pub fn parse(bytes: &[u8]) -> Option<Self> {
        if bytes.len() < Self::BYTES {
            return None;
        }

        let capture_uptime_ms = u32::from_le_bytes([bytes[0], bytes[1], bytes[2], bytes[3]]);
        let freq_mhz = u16::from_le_bytes([bytes[4], bytes[5]]);
        let rssi_byte = bytes[6] as i8;
        let rate_flags = bytes[8];
        let rate_code = bytes[9];

        let rssi_dbm = if rssi_byte == 0 {
            None
        } else {
            Some(rssi_byte as f32)
        };

        Some(Self {
            capture_uptime_ms,
            freq_mhz,
            rssi_dbm,
            rate_flags,
            rate_code,
        })
    }
}