use strum_macros::{EnumIter, Display};

#[derive(Debug, Clone, EnumIter, Display)]
pub enum ChannelName {
    EthernetBroker,
    Dot11Broker,
    EthernetPipeline,
    ArpPipeline,
    TcpPipeline,
    UdpPipeline,
    DnsPipeline
}
