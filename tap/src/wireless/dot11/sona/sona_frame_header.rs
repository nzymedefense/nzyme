#[derive(Debug, Clone, Copy)]
pub struct SonaFrameHeader {
    pub freq_mhz: u16,
    pub rssi_dbm: Option<f32>,
    pub rate_flags: u8,
    pub rate_code: u8,
}

impl SonaFrameHeader {
    pub const BYTES: usize = 6;

    #[inline]
    pub fn decode_rssi_dbm(raw: u8) -> Option<f32> {
        // Sentinel values for "no RSSI available".
        if raw == 0x00 || raw == 0x80 || raw == 0xff {
            return None;
        }

        // Interpret as signed i8.
        Some((raw as i8) as f32)
    }

    pub fn parse(b: &[u8]) -> Option<Self> {
        if b.len() < Self::BYTES { return None; }

        let freq_mhz = u16::from_le_bytes([b[0], b[1]]);
        let rssi_dbm  = Self::decode_rssi_dbm(b[2]);

        Some(Self {
            freq_mhz,
            rssi_dbm,
            rate_flags: b[4],
            rate_code:  b[5],
        })
    }
}