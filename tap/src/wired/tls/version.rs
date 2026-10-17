use std::fmt;
use std::fmt::Formatter;

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub enum TlsVersion {
    Ssl20,
    Ssl30,
    Tls10,
    Tls11,
    Tls12,
    Tls13,
    Unknown(u16)
}

impl TlsVersion {
    pub fn from_u16(v: u16) -> Self {
        match v {
            0x0002 => TlsVersion::Ssl20,
            0x0300 => TlsVersion::Ssl30,
            0x0301 => TlsVersion::Tls10,
            0x0302 => TlsVersion::Tls11,
            0x0303 => TlsVersion::Tls12,
            0x0304 => TlsVersion::Tls13,
            other => TlsVersion::Unknown(other)
        }
    }

    pub fn as_u16(&self) -> u16 {
        match self {
            TlsVersion::Ssl20 => 0x0002,
            TlsVersion::Ssl30 => 0x0300,
            TlsVersion::Tls10 => 0x0301,
            TlsVersion::Tls11 => 0x0302,
            TlsVersion::Tls12 => 0x0303,
            TlsVersion::Tls13 => 0x0304,
            TlsVersion::Unknown(v) => *v
        }
    }
}

impl From<rustls::ProtocolVersion> for TlsVersion {
    fn from(v: rustls::ProtocolVersion) -> Self {
        TlsVersion::from_u16(u16::from(v))
    }
}

impl fmt::Display for TlsVersion {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        match self {
            TlsVersion::Ssl20 => write!(f, "SSLv2"),
            TlsVersion::Ssl30 => write!(f, "SSLv3"),
            TlsVersion::Tls10 => write!(f, "TLSv1.0"),
            TlsVersion::Tls11 => write!(f, "TLSv1.1"),
            TlsVersion::Tls12 => write!(f, "TLSv1.2"),
            TlsVersion::Tls13 => write!(f, "TLSv1.3"),
            TlsVersion::Unknown(v) => write!(f, "Unknown(0x{:04x})", v)
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn round_trips_wire_values() {
        for v in [0x0002, 0x0300, 0x0301, 0x0302, 0x0303, 0x0304, 0x0305] {
            assert_eq!(TlsVersion::from_u16(v).as_u16(), v);
        }
    }

    #[test]
    fn converts_from_rustls() {
        assert_eq!(TlsVersion::from(rustls::ProtocolVersion::TLSv1_3), TlsVersion::Tls13);
        assert_eq!(TlsVersion::from(rustls::ProtocolVersion::TLSv1_2), TlsVersion::Tls12);
        assert_eq!(TlsVersion::Tls13.to_string(), "TLSv1.3");
        assert_eq!(TlsVersion::Unknown(0x0305).to_string(), "Unknown(0x0305)");
    }
}
