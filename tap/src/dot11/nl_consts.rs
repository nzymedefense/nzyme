pub const NL_80211_GENL_NAME: &str = "nl80211";
pub const NL_80211_GENL_VERSION: u8 = 1;

#[neli::neli_enum(serialized_type = "u8")]
pub enum Nl80211Command {
    Unspecified = 0,
    GetWiPhy = 1,
    GetIf = 5,
    SetIf = 6,
    SetChannel = 65
}
impl neli::consts::genl::Cmd for Nl80211Command {}

#[neli::neli_enum(serialized_type = "u16")]
pub enum Nl80211Attribute {
    /* don't change the order or add anything between, this is ABI! */
    Unspecified = 0,

    WiPhy = 1,
    WiphyName = 2,

    IfIndex = 3,
    IfName = 4,
    IfType = 5,

    Mac = 6,

    Keydata = 7,
    KeyIdx = 8,
    KeyCipher = 9,
    KeySeq = 10,
    KeyDefault = 11,

    BeaconInternal = 12,
    DtimPeriod = 13,
    BeaconHead = 14,
    BeaconTail = 15,

    StaAid = 16,
    StaFlags = 17,
    StaListenInterval = 18,
    StaSupportedRates = 19,
    StaVlan = 20,
    StaInfo = 21,

    WiPhyBands = 22,

    MntrFlags = 23,

    MeshId = 24,
    StaPlinkAction = 25,
    MpathNextHop = 26,
    MpathInfo = 27,

    BssCtsProt = 28,
    BssShortPreamble = 29,
    BssShortSlotTime = 30,

    HtCapability = 31,

    SupportedIftypes = 32,

    RegAlpha2 = 33,
    RegRules = 34,

    MeshConfig = 35,

    BssBasicRates = 36,

    WiphyTxqParams = 37,
    WiPhyFreq = 38,
    WiphyChannelType = 39,

    KeyDefaultMgmt = 40,

    MgmtSubtype = 41,
    Ie = 42,

    MaxNumScanSsids = 43,

    ScanFrequencies = 44,
    ScanSsids = 45,
    Generation = 46, /* replaces old SCAN_GENERATION */
    Bss = 47,

    RegInitiator = 48,
    RegType = 49,

    SupportedCommands = 50,

    Frame = 51,
    Ssid = 52,
    AuthType = 53,
    ReasonCode = 54,

    KeyType = 55,

    MaxScanIeLen = 56,
    CipherSuites = 57,

    FreqBefore = 58,
    FreqAfter = 59,

    FreqFixed = 60,

    WiphyRetyShort = 61,
    WiphyRetryLong = 62,
    WiphyFragThreshold = 63,
    WiphyRtsThreshold = 64,

    TimedOut = 65,

    UseMfp = 66,

    StaFlags2 = 67,

    ControlPort = 68,

    TestData = 69,

    Privacy = 70,

    DisconnectedByAp = 71,
    StatusCode = 72,

    CipherSuitesPairwise = 73,
    CipherSuiteGroup = 74,
    WapVersions = 75,
    AkmSuites = 76,

    ReqIe = 77,
    RespIe = 78,

    PrevBssid = 79,

    AttrKey = 80,
    AttrKeys = 81,

    AttrPid = 82,

    Attr4addr = 83,

    SurveyInfo = 84,

    Pmkid = 85,
    MaxNumPmkids = 86,

    Duration = 87,

    Cookie = 88,

    WiphyCoverageClass = 89,

    TxRates = 90,

    FrameMatch = 91,

    Ack = 92,

    PsState = 93,

    Cqm = 94,

    LocalStateChange = 95,

    ApIsolate = 96,

    WiphyTxPowerSetting = 97,
    WiphyTxPowerLevel = 98,

    TxFrameTypes = 99,
    RxFrameTypes = 100,
    FrameType = 101,

    ControlPortEthertype = 102,
    ControlPortNoEncrypt = 103,

    SupportIbssRsn = 104,

    WiphyAntennaTx = 105,
    WiphyAntennaRx = 106,

    McastRate = 107,

    OffChannelTxOk = 108,

    BssHtOpmode = 109,

    KeyDefaultTypes = 110,

    MaxRemaisOnChannelDuration = 111,

    MeshSetup = 112,

    WiphyAntennaAvailTx = 113,
    WiphyAntennaAvailRx = 114,

    SupportMeshAuth = 115,
    StaPlinkState = 116,

    WowlanTriggers = 117,
    WowlanTriggersSupported = 118,

    SchedScanInterval = 119,

    InterfaceCombinations = 120,
    SoftwareIftypes = 121,

    RekeyData = 122,

    MaxNumSchedScanSsids = 123,
    MaxSchedScanIeLen = 124,

    ScanSuppRates = 125,

    HiddenSsid = 126,

    IeProbeResp = 127,
    IeAssocResp = 128,

    StaWme = 129,
    SupportApUapsd = 130,

    RoamSupport = 131,

    SchedScanMatch = 132,
    MaxMatchSets = 133,

    PmksaCandidate = 134,

    TxNoCckRate = 135,

    TdlsAction = 136,
    TdlsDialogToken = 137,
    TdlsOperation = 138,
    TdlsSupport = 139,
    TdlsExternalSetup = 140,

    DeviceApSme = 141,

    DontWaitForAck = 142,

    FeatureFlags = 143,

    ProbeRespOffload = 144,

    ProbeResp = 145,

    DfsRegion = 146,

    DisableHt = 147,
    HtCapabilityMask = 148,

    NoackMap = 149,

    InactivityTimeout = 150,

    RxSignalDbm = 151,

    BgScanPeriod = 152,

    Wdev = 153,

    UserRegHintType = 154,

    ConnFailedReason = 155,

    AuthData = 156,

    VhtCapability = 157,

    ScanFlags = 158,

    ChannelWidth = 159,
    CenterFreq1 = 160,
    CenterFreq2 = 161,

    P2pCtwindow = 162,
    P2pOppps = 163,

    LocalMeshPowerMode = 164,

    AclPolicy = 165,

    MacAddrs = 166,

    MacAclMax = 167,

    RadarEvent = 168,

    ExtCapa = 169,
    ExtCapaMask = 170,

    StaCapability = 171,
    StaExtCapability = 172,

    ProtocolFeatures = 173,
    SplitWiphyDump = 174,

    DisableVht = 175,
    VhtCapabilityMask = 176,

    Mdid = 177,
    IeRic = 178,

    CritProtId = 179,
    MaxCritProtDuration = 180,

    PeerAid = 181,

    CoalesceRule = 182,

    ChSwitchCount = 183,
    ChSwitchBlockTx = 184,
    CasIes = 185,
    CntdwnOffsBeacon = 186,
    CntdwnOffsPresp = 187,

    RxmgmtFlags = 188,

    StaSupportedChannels = 189,

    StaSupportedOperClasses = 190,

    HandleDfs = 191,

    Support5Mhz = 192,
    Support10Mhz = 193,

    OpmodeNotif = 194,

    VendorId = 195,
    VendorSubcmd = 196,
    VendorData = 197,
    VendorEvents = 198,

    QosMap = 199,

    MacHint = 200,
    WiphyFreqHint = 201,

    MaxApAssocSta = 202,

    TdlsPeerCapability = 203,

    SocketOwner = 204,

    CasCOffsetsTx = 205,
    MaxCasCounters = 206,

    TdlsInitiator = 207,

    UseRrm = 208,

    WiphyDynAck = 209,

    Tsid = 210,
    UserPrio = 211,
    AdmittedTime = 212,

    SmpsMode = 213,

    OperClass = 214,

    MacMask = 215,

    WiphySelfManagedRed = 216,

    ExtFeatures = 217,

    SurveyRadioStats = 218,

    NetnsFd = 219,

    SchedScanDelay = 220,

    RegIndoor = 221,

    MaxNumSchedScanPlans = 222,
    MaxScanPlanInterval = 223,
    MaxScanPlanIterations = 224,
    SchedScanPlans = 225,

    Pbss = 226,

    BssSelect = 227,

    StaSupportP2pPs = 228,

    Pad = 229,

    IftypeExtCapa = 230,

    MuMimoGroupData = 231,
    MuMimoFollowMacAddr = 232,

    ScanStartTimeTsf = 233,
    ScanStartTimeTsfBssid = 234,
    MeasurementDuration = 235,
    MeasurementDurationMandatory = 236,

    MeshPeerAid = 237,

    NanMasterPref = 238,
    Bands = 239,
    NanFunc = 240,
    NanMatch = 241,

    FilsKek = 242,
    FilsNonces = 243,

    MulticastToUnicastEnabled = 244,

    Bssid = 245,

    SchedScanRelativeRssi = 246,
    SchedScanRssiAdjust = 247,

    TimeoutReason = 248,

    FilsErpUsername = 249,
    FilsErpRealm = 250,
    FilsErpNextSeqNum = 251,
    FilsErpRrk = 252,
    FilsCacheId = 253,

    Pmk = 254,

    SchedScanMulti = 255,
    SchedScanMaxReqs = 256,

    Want1x4wayHs = 257,
    Pmkr0Name = 258,
    PortAuthorized = 259,

    ExternalAuthAction = 260,
    ExternalAuthSupport = 261,

    Nss = 262,
    AckSignal = 263,

    ControlPortOverNl80211 = 264,

    TxqStats = 265,
    TxqLimit = 266,
    TxqMemoryLimit = 267,
    TxqQuantum = 268,

    HeCapability = 269,

    FtmResponder = 270,

    FtmResponderStats = 271,

    Timeout = 272,

    PeerMeasurements = 273,

    AirtimeWeigth = 274,
    StaTxPowerSettings = 275,
    StaTxPower = 276,

    SaePassword = 277,

    TwtResponder = 278,

    HeObssPd = 279,

    WiphyEdmgChannels = 280,
    WiphyEdmgBwConfig = 281,

    VlanId = 282,

    HeBssColor = 283,

    IftypeAkmSuites = 284,

    TidConfig = 285,

    ControlPortNoPreauth = 286,

    PmkLifetime = 287,
    PmkPreauthThreshold = 288,

    ReceiveMulticast = 289,
    WiphyFreqOffset = 290,
    CenterFreq1Offset = 291,
    ScanFreqKhz = 292,

    He6ghzCapability = 293,

    FilsDiscovery = 294,

    UnsolBcastProbeResp = 295,

    S1gCapability = 296,
    S1gCapabilityMask = 297,

    SaePwe = 298,

    ReconnectRequested = 299,

    SarSpec = 300,

    DisableHe = 301,

    ObssColorBitmap = 302,

    ColorChangeCount = 303,
    ColorChangeColor = 304,
    ColorChangeElems = 305,

    MbssidConfig = 306,
    MbssidElems = 307,

    RadarBackground = 308,

    ApSettingsFlags = 309,

    EhtCapability = 310,

    DisableEht = 311,

    MloLinks = 312,
    MliLinkId = 313,
    MldAddr = 314,

    MloSupport = 315,

    MaxnumAkmSuites = 316,

    EmlCapability = 317,
    MldCapaAndOps = 318,

    TxHwTimestamp = 319,
    RxHwTimestamp = 320,
    TdBitmap = 321,

    PunctBitmap = 322,

    MaxHwTimestampPeers = 323,
    HwTimestampEnabled = 324,

    EmaRnrElems = 325,

    MloLinkDisabled = 326,

}

impl neli::consts::genl::NlAttrType for Nl80211Attribute {}

#[neli::neli_enum(serialized_type = "u16")]
pub enum Nl80211BandAttr {
    Invalid = 0,
    Freqs = 1,
    Rates = 2,
    HtMcsSet = 3,
    HtCapa = 4,
    HtAmpduFactor = 5,
    HtAmpduDensity = 6,
    VhtMcsSet = 7,
    VhtCapa = 8,
    IftypeData = 9,
    EdmgChannels = 10,
    EdmgBwConfig = 11,
    S1gMcsNssSet = 12,
    S1gCapa = 13,
}

impl neli::consts::genl::NlAttrType for Nl80211BandAttr {}

#[neli::neli_enum(serialized_type = "u16")]
pub enum Nl80211FrequencyAttr {
    Invalid = 0,
    Freq = 1,
    Disabled = 2,
    NoIr = 3,
    NoIbss = 4,
    Radar = 5,
    MaxTxPower = 6,
    DfsState = 7,
    DfsTime = 8,
    NoHt40Minus = 9,
    NoHt40Plus = 10,
    No80Mhz = 11,
    No160Mhz = 12,
    DfsCacTime = 13,
    IndoorOnly = 14,
    IrConcurrent = 15,
    No20Mhz = 16,
    No10Mhz = 17,
    Wmm = 18,
    NoHe = 19,
    Offset = 20,
    Mhz1 = 21,
    Mhz2 = 22,
    Mhz4 = 23,
    Mhz8 = 24,
    Mhz16 = 25,
    No320Mhz = 26,
    NoEht = 27,
}

impl neli::consts::genl::NlAttrType for Nl80211FrequencyAttr {}

#[neli::neli_enum(serialized_type = "u16")]
pub enum Nl80211MntrFlags {
    Control = 3,
    OtherBss = 4
}
impl neli::consts::genl::NlAttrType for Nl80211MntrFlags {}