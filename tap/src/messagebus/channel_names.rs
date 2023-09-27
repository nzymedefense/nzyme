use strum_macros::{EnumIter, Display};

#[derive(Debug, Clone, EnumIter, Display)]
pub enum ChannelName {
    EthernetBroker,
    Dot11Broker,

    Dot11FramesPipeline,

    EthernetPipeline,
    ArpPipeline,
    TcpPipeline,
    UdpPipeline,
    DnsPipeline,

    Dot11BeaconOutputPipeline,
    DnsOutputPipeline
}
