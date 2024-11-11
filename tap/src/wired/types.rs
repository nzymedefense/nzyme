use strum_macros::{Display};

pub struct NotImplementedError;

#[derive(Debug)]
pub enum HardwareType {
    Ethernet,
    NotImplemented
}

pub fn find_hardwaretype(val: u16) -> HardwareType {
    match val {
        1 => HardwareType::Ethernet,
        _ => HardwareType::NotImplemented
    }
}

#[derive(Debug, PartialEq)]
pub enum EtherType {
    IPv4,
    Arp,
    IPv6,
    NotImplemented
}

pub fn find_ethertype(val: u16) -> EtherType {
    match val {
        2048 => EtherType::IPv4,
        2054 => EtherType::Arp,
        _ => EtherType::NotImplemented
    }
}

#[derive(Debug)]
pub enum ARPOpCode {
    Request,
    Reply,
    NotImplemented
}

pub fn find_arp_opcode(val: u16) -> ARPOpCode {
    match val {
        1 => ARPOpCode::Request,
        2 => ARPOpCode::Reply,
        _ => ARPOpCode::NotImplemented
    }
}

#[derive(Debug)]
pub enum ProtocolType {
    Tcp,
    Udp,
}

impl TryFrom<u8> for ProtocolType {
    type Error = NotImplementedError;

    fn try_from(val: u8) -> Result<Self, Self::Error> {
        match val {
            6 => Ok(ProtocolType::Tcp),
            17 => Ok(ProtocolType::Udp),
            _ => Err(NotImplementedError)
        }
    }
}

#[derive(Debug, Eq, PartialEq)]
pub enum DNSType {
    Query,
    QueryResponse
}

#[allow(clippy::upper_case_acronyms)]
#[derive(Debug, Display, Eq, PartialEq)]
pub enum DNSDataType {
    A,
    NS,
    MD,
    MF,
    CNAME,
    SOA,
    MB,
    MG,
    MR,
    NULL,
    WKS,
    PTR,
    HINFO,
    MINFO,
    MX,
    TXT,
    RP,
    AFSDB,
    SIG,
    KEY,
    AAAA,
    LOC,
    SRV,
    NAPTR,
    KX,
    CERT,
    DNAME,
    APL,
    DS,
    SSHFP,
    IPSECKEY,
    RRSIG,
    NSEC,
    DNSKEY,
    DHCID,
    NSEC3,
    NSEC3PARAM,
    TLSA,
    SMIMEA,
    HIP,
    CDS,
    CDNSKEY,
    OPENPGPKEY,
    CSYNC,
    ZONEMD,
    SVCB,
    HTTPS,
    EUI48,
    EUI64,
    TKEY,
    TSIG,
    URI,
    CAA,
    TA,
    DLV
}

impl TryFrom<u16> for DNSDataType {
    type Error = NotImplementedError;

    fn try_from(val: u16) -> Result<Self, Self::Error> {
        match val {
            1     => Ok(DNSDataType::A),
            2     => Ok(DNSDataType::NS),
            3     => Ok(DNSDataType::MD),
            4     => Ok(DNSDataType::MF),
            5     => Ok(DNSDataType::CNAME),
            6     => Ok(DNSDataType::SOA),
            7     => Ok(DNSDataType::MB),
            8     => Ok(DNSDataType::MG),
            9     => Ok(DNSDataType::MR),
            10    => Ok(DNSDataType::NULL),
            11    => Ok(DNSDataType::WKS),
            12    => Ok(DNSDataType::PTR),
            13    => Ok(DNSDataType::HINFO),
            14    => Ok(DNSDataType::MINFO),
            15    => Ok(DNSDataType::MX),
            16    => Ok(DNSDataType::TXT),
            17    => Ok(DNSDataType::RP),
            18    => Ok(DNSDataType::AFSDB),
            24    => Ok(DNSDataType::SIG),
            25    => Ok(DNSDataType::KEY),
            28    => Ok(DNSDataType::AAAA),
            29    => Ok(DNSDataType::LOC),
            33    => Ok(DNSDataType::SRV),
            35    => Ok(DNSDataType::NAPTR),
            36    => Ok(DNSDataType::KX),
            37    => Ok(DNSDataType::CERT),
            39    => Ok(DNSDataType::DNAME),
            42    => Ok(DNSDataType::APL),
            43    => Ok(DNSDataType::DS),
            44    => Ok(DNSDataType::SSHFP),
            45    => Ok(DNSDataType::IPSECKEY),
            46    => Ok(DNSDataType::RRSIG),
            47    => Ok(DNSDataType::NSEC),
            48    => Ok(DNSDataType::DNSKEY),
            49    => Ok(DNSDataType::DHCID),
            50    => Ok(DNSDataType::NSEC3),
            51    => Ok(DNSDataType::NSEC3PARAM),
            52    => Ok(DNSDataType::TLSA),
            53    => Ok(DNSDataType::SMIMEA),
            55    => Ok(DNSDataType::HIP),
            59    => Ok(DNSDataType::CDS),
            60    => Ok(DNSDataType::CDNSKEY),
            61    => Ok(DNSDataType::OPENPGPKEY),
            62    => Ok(DNSDataType::CSYNC),
            63    => Ok(DNSDataType::ZONEMD),
            64    => Ok(DNSDataType::SVCB),
            65    => Ok(DNSDataType::HTTPS),
            108   => Ok(DNSDataType::EUI48),
            109   => Ok(DNSDataType::EUI64),
            249   => Ok(DNSDataType::TKEY),
            250   => Ok(DNSDataType::TSIG),
            256   => Ok(DNSDataType::URI),
            257   => Ok(DNSDataType::CAA),
            32768 => Ok(DNSDataType::TA),
            32769 => Ok(DNSDataType::DLV),
            _  => Err(NotImplementedError)
        }
    }
}

#[derive(Debug)]
pub enum DNSClass {
    IN,
    CS,
    CH,
    HS
}

impl TryFrom<u16> for DNSClass {
    type Error = NotImplementedError;
    
    fn try_from(val: u16) -> Result<Self, Self::Error> {
        match val {
            1 => Ok(DNSClass::IN),
            2 => Ok(DNSClass::CS),
            3 => Ok(DNSClass::CH),
            4 => Ok(DNSClass::HS),
            _  => Err(NotImplementedError)
        }
    }

}

#[derive(Debug)]
pub enum DHCPv4OpCode {
    Request,
    Reply
}

#[derive(Debug, Eq, PartialEq)]
pub enum DHCPv4MessageType {
    Discover,
    Offer,
    Request,
    Decline,
    Ack,
    Nack,
    Release,
    Inform,
    ForceRenew,
    LeaseQuery,
    LeaseUnassigned,
    LeaseUnknown,
    LeaseActive,
    BulkLeaseQuery,
    LeaseQueryDone,
    LeaseQueryData,
    Unknown
}


impl TryFrom<u8> for DHCPv4MessageType {
    type Error = ();

    fn try_from(val: u8) -> Result<Self, Self::Error> {
        match val {
            1  => Ok(DHCPv4MessageType::Discover),
            2  => Ok(DHCPv4MessageType::Offer),
            3  => Ok(DHCPv4MessageType::Request),
            4  => Ok(DHCPv4MessageType::Decline),
            5  => Ok(DHCPv4MessageType::Ack),
            6  => Ok(DHCPv4MessageType::Nack),
            7  => Ok(DHCPv4MessageType::Release),
            8  => Ok(DHCPv4MessageType::Inform),
            9  => Ok(DHCPv4MessageType::ForceRenew),
            10 => Ok(DHCPv4MessageType::LeaseQuery),
            11 => Ok(DHCPv4MessageType::LeaseUnassigned),
            12 => Ok(DHCPv4MessageType::LeaseUnknown),
            13 => Ok(DHCPv4MessageType::LeaseActive),
            14 => Ok(DHCPv4MessageType::BulkLeaseQuery),
            15 => Ok(DHCPv4MessageType::LeaseQueryDone),
            16 => Ok(DHCPv4MessageType::LeaseQueryData),
            _  => Ok(DHCPv4MessageType::Unknown),
        }
    }
}