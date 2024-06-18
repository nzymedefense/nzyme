use strum_macros::{EnumIter, Display};

#[derive(Debug, Clone, EnumIter, Display)]
pub enum EthernetChannelName {
    EthernetBroker,
    ArpPipeline,
    TcpPipeline,
    UdpPipeline,
    DnsPipeline,
    SocksPipeline,
    SshPipeline
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