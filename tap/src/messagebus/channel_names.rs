use strum_macros::{EnumIter, Display};

#[derive(Debug, Clone, EnumIter, Display)]
pub enum WiredChannelName {
    EthernetBroker,
    ArpPipeline,
    TcpPipeline,
    UdpPipeline,
    DnsPipeline,
    SocksPipeline,
    SshPipeline,
    Dhcpv4Pipeline,
    NtpPipeline
}

#[derive(Debug, Clone, EnumIter, Display)]
pub enum Dot11ChannelName {
    Dot11Broker,
    Dot11FramesPipeline,
}

#[derive(Debug, Clone, EnumIter, Display)]
pub enum BluetoothChannelName {
    BluetoothDevicesPipeline,
}

#[derive(Debug, Clone, EnumIter, Display)]
pub enum GenericChannelName {
    UavRemoteIdPipeline,
    GnssNmeaMessagesPipeline,
    GnssUbxMonRfPipeline,
    GnssUbxRxmMeasxPipeline
}
