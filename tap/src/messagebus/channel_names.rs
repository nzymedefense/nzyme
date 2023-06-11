use strum_macros::{EnumIter, Display};

#[derive(Debug, Clone, EnumIter, Display)]
pub enum ChannelName {
    EthernetBroker,
    Dot11Broker,

    Dot11ManagementFramePipeline,
    Dot11IgnoredFramePipeline,

    EthernetPipeline,
    ArpPipeline,
    TcpPipeline,
    UdpPipeline,
    DnsPipeline
}
