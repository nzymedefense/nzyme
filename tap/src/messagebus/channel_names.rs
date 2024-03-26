use strum_macros::{EnumIter, Display};

#[derive(Debug, Clone, EnumIter, Display)]
pub enum EthernetChannelName {
    EthernetBroker,
    EthernetPipeline,
    ArpPipeline,
    TcpPipeline,
    UdpPipeline,
    DnsPipeline
}


#[derive(Debug, Clone, EnumIter, Display)]
pub enum Dot11ChannelName {
    Dot11Broker,
    Dot11FramesPipeline,
}