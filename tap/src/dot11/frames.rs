use strum_macros::Display;

#[derive(Debug)]
pub struct Dot11RawFrame {
    pub interface_name: String,
    pub data: Vec<u8>
}

#[derive(Debug, Clone)]
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

#[derive(Debug, Display, Eq, PartialEq, Hash)]
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
    pub length: usize
}

#[derive(Debug)]
pub struct BeaconCapabilities {
    pub infrastructure_type: InfraStructureType,
    pub privacy: bool,
    pub short_preamble: bool,
    pub pbcc: bool,
    pub channel_agility: bool,
    pub short_slot_time: bool,
    pub dsss_ofdm: bool
}

#[derive(Debug)]
pub struct BeaconTaggedParameters {
    pub ssid: Option<String>,
    pub supported_rates: Option<Vec<f32>>,
    pub extended_supported_rates: Option<Vec<f32>>,
    pub country_information: Option<CountryInformation>,
    
    // Not parsing, only keeping for fingerprint calculation.
    pub ht_capabilities: Option<Vec<u8>>,
    pub extended_capabilities: Option<Vec<u8>>
}

#[derive(Debug)]
pub struct CountryInformation {
    pub country_code: String,
    pub environment: RegulatoryEnvironment,
    pub first_channel: u8,
    pub channel_count: u8,
    pub max_transmit_power: u8
}

#[derive(Debug)]
pub enum RegulatoryEnvironment {
    All,
    Indoors,
    Outdoors,
    Unknown
}

#[derive(Debug, Display, Eq, PartialEq)]
pub enum InfraStructureType {
    Invalid,
    AccessPoint,
    AdHoc
}

#[derive(Debug)]
pub struct SecurityInformation {
    pub protocols: Vec<EncryptionProtocol>,
    pub suites: Option<CipherSuites> // Optional in case protocol is WEP
}

#[derive(Debug, Display, Copy, Clone)]
pub enum EncryptionProtocol {
    WEP,
    WPA1,
    WPA2,
    WPA3
}

#[derive(Debug)]
pub struct CipherSuites {
    pub group_cipher: CipherSuite,
    pub pairwise_ciphers: Vec<CipherSuite>,
    pub key_management_modes: Vec<KeyManagementMode>
}

#[derive(Debug, Display, PartialEq)]
pub enum KeyManagementMode {
    Unknown,
    X802_1,
    PSK,
    SAE,
    FT802_1X,
    FTPSK,
    FTSAE
}

#[derive(Debug, Display)]
pub enum CipherSuite {
    None,
    Unknown,
    WEP,
    TKIP,
    CCMP,
    WEP104,
    BIPCMAC128,
    GCMP128,
    GCMP256,
    CCMP256,
    BIPGMAC128,
    BIPGMAC256,
    BIPCMAC256
}

#[derive(Debug)]
pub struct Dot11BeaconFrame {
    pub length: usize,
    pub header: RadiotapHeader,
    pub destination: String,
    pub transmitter: String,
    pub timestamp: u64,
    pub interval: u16,
    pub capabilities: BeaconCapabilities,
    pub ssid: Option<String>,
    pub tagged_parameters: BeaconTaggedParameters,
    pub fingerprint: String,
    pub security: Vec<SecurityInformation>,
    pub has_wps: bool
}

#[derive(Debug)]
pub struct Dot11ProbeRequestFrame {
    pub length: usize,
    pub header: RadiotapHeader,
    pub transmitter: String,
    pub ssid: Option<String>,
}

#[derive(Debug)]
pub struct Dot11DataFrame {
    pub length: usize,
    pub header: RadiotapHeader,
    pub ds: Dot11DSInformation
}

#[derive(Debug)]
pub struct Dot11DeauthenticationFrame {
    pub length: usize,
    pub header: RadiotapHeader,
    pub destination: String,
    pub transmitter: String,
    pub bssid: String,
    pub reason_code: u16
}

#[derive(Debug)]
pub struct Dot11DSInformation {
    pub destination: String,
    pub source: String,
    pub bssid: String,
    pub direction: Dot11DataFrameDirection
}

#[derive(Debug, PartialEq, Eq)]
pub enum Dot11DataFrameDirection {
    Entering,
    Leaving,
    NotLeavingOrAdHoc,
    WDS
}