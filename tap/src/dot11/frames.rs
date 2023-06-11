#[derive(Debug)]
pub struct Dot11RawFrame {
    pub interface_name: String,
    pub data: Vec<u8>
}

#[derive(Debug)]
pub struct RadiotapHeader {
    pub is_wep: Option<bool>,
    pub data_rate: Option<u16>,
    pub frequency: Option<u16>,
    pub channel: Option<u16>,
    pub antenna_signal: Option<i8>,
    pub antenna: Option<u8>
}

#[derive(Debug)]
pub struct RadiotapHeaderPresentFlags {
    pub tsft: bool,
    pub flags: bool,
    pub rate: bool,
    pub channel: bool,
    pub fhss: bool,
    pub antenna_signal_dbm: bool,
    pub antenna_noise_dbm: bool,
    pub lock_quality: bool,
    pub tx_attenuation: bool,
    pub tx_attenuation_db: bool,
    pub tx_power_dbm: bool,
    pub antenna: bool,
    pub antenna_signal_db: bool,
    pub antenna_noise_db: bool,
    pub rx_flags: bool,
    pub tx_flags: bool,
    pub data_retries: bool,
    pub channel_plus: bool,
    pub mcs: bool,
    pub ampdu: bool,
    pub vht: bool,
    pub timestamp: bool,
    pub he_info: bool,
    pub hemu_info: bool,
    pub zero_length_psdu: bool,
    pub lsig: bool,
    pub tlvs: bool,
    pub radiotap_ns_next: bool,
    pub vendor_nx_next: bool,
    pub ext: bool
}

#[derive(Debug)]
pub struct RadiotapHeaderFlags {
    pub cfp: bool,
    pub preamble: bool,
    pub wep: bool,
    pub fragmentation: bool,
    pub fcs_at_end: bool,
    pub data_pad: bool,
    pub bad_fcs: bool,
    pub short_gi: bool
}

pub struct FrameTypeInformation {
    pub frame_type: FrameType,
    pub frame_subtype: FrameSubType
}

pub enum FrameType {
    Management,
    Control,
    Data,
    Extension,
    Invalid
}

#[derive(Debug)]
pub enum FrameSubType {
    AssociationRequest,
    AssociationResponse,
    ReAssociationRequest,
    ReAssociationResponse,
    ProbeRequest,
    ProbeResponse,
    TimingAdvertisement,
    Beacon,
    Atim,
    Disassocation,
    Authentication,
    Deauthentication,
    Action,
    ActionNoAck,
    Trigger,
    Tack,
    BeamformingReportPoll,
    VhtHeNdpAnnouncement,
    ControlFrameExtension,
    ControlWrapper,
    BlockAckRequest,
    BlockAck,
    PsPoll,
    Rts,
    Cts,
    Ack,
    CfEnd,
    CfEndCfAck,
    Data,
    Null,
    QosData,
    QosDataCfAck,
    QosDataCfPoll,
    QosDataCfAckCfPoll,
    QosNull,
    QosCfPoll,
    QosCfAckCfPoll,
    DmgBeacon,
    S1gBeacon,
    Reserved,
    Invalid
}


#[derive(Debug)]
pub struct Dot11Frame {
    pub header: RadiotapHeader,
    pub frame_type: FrameSubType,
    pub payload: Vec<u8>,
}