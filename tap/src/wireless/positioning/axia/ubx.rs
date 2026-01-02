use crate::wireless::positioning::gnss_constellation::GNSSConstellation;

#[derive(Debug)]
pub struct UbxMonRfBlock {
    pub block_id: u8,
    pub flags: u8,
    pub jamming_state: JammingState,
    pub antenna_status: AntennaStatus,
    pub antenna_power: AntennaPower,
    pub post_status: u32,
    pub noise_per_ms: u16,
    pub agc_cnt: u16,
    pub jam_ind: u8,
    pub ofs_i: i8,
    pub mag_i: u8,
    pub ofs_q: i8,
    pub mag_q: u8,
}

#[derive(Debug)]
pub struct UbxMonRfMessage {
    pub version: u8,
    pub constellation: GNSSConstellation,
    pub blocks: Vec<UbxMonRfBlock>,
}

impl UbxMonRfMessage {
    pub fn estimate_size(&self) -> usize {
        21
    }
}

#[derive(Debug, Clone, Copy)]
pub enum JammingState {
    UnknownOrDisabled,
    OkNoSignificant,
    Warning,
    Critical,
    Other(u8),
}

impl JammingState {
    pub fn from_flags(flags: u8) -> Self {
        match flags & 0b11 {
            0 => JammingState::UnknownOrDisabled,
            1 => JammingState::OkNoSignificant,
            2 => JammingState::Warning,
            3 => JammingState::Critical,
            other => JammingState::Other(other),
        }
    }
}

#[derive(Debug, Clone, Copy)]
pub enum AntennaStatus {
    Init,
    DontKnow,
    Ok,
    Short,
    Open,
    Other(u8),
}

impl AntennaStatus {
    pub fn from_u8(v: u8) -> Self {
        match v {
            0x00 => AntennaStatus::Init,
            0x01 => AntennaStatus::DontKnow,
            0x02 => AntennaStatus::Ok,
            0x03 => AntennaStatus::Short,
            0x04 => AntennaStatus::Open,
            other => AntennaStatus::Other(other),
        }
    }
}

#[derive(Debug, Clone, Copy)]
pub enum AntennaPower {
    Off,
    On,
    DontKnow,
    Other(u8),
}

impl AntennaPower {
    pub fn from_u8(v: u8) -> Self {
        match v {
            0x00 => AntennaPower::Off,
            0x01 => AntennaPower::On,
            0x02 => AntennaPower::DontKnow,
            other => AntennaPower::Other(other),
        }
    }
}

#[derive(Debug, Clone)]
pub struct UbxRxmMeasxMessage {
    pub version: u8,
    pub gps_tow_ms: u32,
    pub glo_tow_ms: u32,
    pub bds_tow_ms: u32,
    pub qzss_tow_ms: u32,
    pub gps_tow_acc: u16,
    pub glo_tow_acc: u16,
    pub bds_tow_acc: u16,
    pub qzss_tow_acc: u16,
    pub num_sv: u8,
    pub flags: u8,
    pub tow_set: u8,
    pub sats: Vec<UbxRxmMeasxSat>,
    pub constellation: GNSSConstellation,
}

#[derive(Debug, Clone)]
pub struct UbxRxmMeasxSat {
    pub gnss_id: u8,
    pub sv_id: u8,
    pub sno: u8,
    pub mpath_indic: u8,
    pub doppler_ms: i32,
    pub doppler_hz: i32,
    pub whole_chips: u16,
    pub frac_chips: u16,
    pub code_phase: u32,
    pub int_code_phase: u8,
    pub pseurange_rms_err: u8,
}

impl UbxRxmMeasxMessage {
    pub fn estimate_size(&self) -> usize {
        const FIXED_PAYLOAD_SIZE: usize = 32;
        const PER_SAT_SIZE: usize = 24;

        FIXED_PAYLOAD_SIZE + self.sats.len() * PER_SAT_SIZE
    }
}