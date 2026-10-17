use std::net::{Ipv4Addr, Ipv6Addr};
use chrono::{DateTime, Utc};
use sha2::{Digest, Sha256};
use x509_parser::certificate::X509Certificate;
use x509_parser::extensions::GeneralName;
use x509_parser::objects::{oid2sn, oid_registry};
use x509_parser::oid_registry::Oid;
use x509_parser::prelude::FromDer;
use x509_parser::public_key::PublicKey;
use crate::helpers::encoding::to_hex;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct TlsCertificate {
    pub fingerprint_sha256: String,
    pub der_length: usize,
    pub parsed: bool,
    pub subject: String,
    pub issuer: String,
    pub serial: String,
    pub subject_alternative_names: Vec<String>,
    pub not_before: Option<DateTime<Utc>>,
    pub not_after: Option<DateTime<Utc>>,
    pub self_signed: bool,
    pub is_ca: bool,
    pub public_key_algorithm: String,
    pub public_key_bits: usize,
    pub signature_algorithm: String
}

impl TlsCertificate {
    pub fn from_der(der: &[u8]) -> Self {
        let mut certificate = TlsCertificate {
            fingerprint_sha256: to_hex(&Sha256::digest(der)),
            der_length: der.len(),
            parsed: false,
            subject: String::new(),
            issuer: String::new(),
            serial: String::new(),
            subject_alternative_names: vec![],
            not_before: None,
            not_after: None,
            self_signed: false,
            is_ca: false,
            public_key_algorithm: String::new(),
            public_key_bits: 0,
            signature_algorithm: String::new()
        };

        let Ok((_, cert)) = X509Certificate::from_der(der) else {
            return certificate;
        };

        certificate.parsed = true;
        certificate.subject = cert.subject().to_string();
        certificate.issuer = cert.issuer().to_string();
        certificate.serial = cert.raw_serial_as_string();
        certificate.not_before = DateTime::from_timestamp(cert.validity().not_before.timestamp(), 0);
        certificate.not_after = DateTime::from_timestamp(cert.validity().not_after.timestamp(), 0);
        certificate.self_signed = certificate.subject == certificate.issuer;
        certificate.is_ca = cert.is_ca();
        certificate.signature_algorithm = oid_name(&cert.signature_algorithm.algorithm);

        if let Ok(Some(san)) = cert.subject_alternative_name() {
            for name in &san.value.general_names {
                if let Some(name) = general_name_to_string(name) {
                    certificate.subject_alternative_names.push(name);
                }
            }
        }

        let spki = cert.public_key();
        match spki.parsed() {
            Ok(PublicKey::RSA(key)) => {
                certificate.public_key_algorithm = "RSA".to_string();
                certificate.public_key_bits = key.key_size();
            },
            Ok(PublicKey::EC(point)) => {
                let curve = spki.algorithm.parameters.as_ref()
                    .and_then(|p| p.as_oid().ok())
                    .map(|oid| oid_name(&oid))
                    .unwrap_or_else(|| "unknown curve".to_string());
                certificate.public_key_algorithm = format!("EC {}", curve);
                certificate.public_key_bits = point.key_size();
            },
            Ok(PublicKey::DSA(_)) => certificate.public_key_algorithm = "DSA".to_string(),
            _ => certificate.public_key_algorithm = oid_name(&spki.algorithm.algorithm)
        }

        certificate
    }
}

fn oid_name(oid: &Oid) -> String {
    oid2sn(oid, oid_registry())
        .map(|s| s.to_string())
        .unwrap_or_else(|_| oid.to_string())
}

fn general_name_to_string(name: &GeneralName) -> Option<String> {
    match name {
        GeneralName::DNSName(s) => Some(format!("DNS:{}", s)),
        GeneralName::RFC822Name(s) => Some(format!("email:{}", s)),
        GeneralName::URI(s) => Some(format!("URI:{}", s)),
        GeneralName::IPAddress(bytes) => match bytes.len() {
            4 => Some(format!("IP:{}", Ipv4Addr::new(bytes[0], bytes[1], bytes[2], bytes[3]))),
            16 => {
                let mut octets = [0u8; 16];
                octets.copy_from_slice(bytes);
                Some(format!("IP:{}", Ipv6Addr::from(octets)))
            },
            _ => None
        },
        GeneralName::DirectoryName(dn) => Some(format!("DirName:{}", dn)),
        _ => None
    }
}

#[cfg(test)]
pub mod testdata {
    pub const CA_DER: &[u8] = include_bytes!("testdata/tls_test_ca.der");
    pub const LEAF_DER: &[u8] = include_bytes!("testdata/tls_test_leaf.der");
    pub const LEAF_KEY_DER: &[u8] = include_bytes!("testdata/tls_test_leaf_key.der");
    pub const CA_SHA256: &str = "cdf99a4bd3a5626bc9c1cd447ce2fea8ac8fd5da764e5f25fbf0cc9faa737f47";
    pub const LEAF_SHA256: &str = "60e588b0f2805d5a80f41a738035765dcc14f669afd0c6b705f955387f1e5dcb";
    pub const LEAF_SUBJECT: &str = "C=US, O=nzyme test, CN=tls-test.nzyme.local";
    pub const CA_SUBJECT: &str = "C=US, O=nzyme test, CN=nzyme test CA";
    pub const SERVER_NAME: &str = "tls-test.nzyme.local";
}

#[cfg(test)]
mod tests {
    use super::*;
    use super::testdata::*;

    #[test]
    fn parses_leaf_certificate() {
        let leaf = TlsCertificate::from_der(LEAF_DER);
        assert!(leaf.parsed);
        assert_eq!(leaf.fingerprint_sha256, LEAF_SHA256);
        assert_eq!(leaf.der_length, LEAF_DER.len());
        assert_eq!(leaf.subject, LEAF_SUBJECT);
        assert_eq!(leaf.issuer, CA_SUBJECT);
        assert!(!leaf.self_signed);
        assert!(!leaf.is_ca);
        assert_eq!(leaf.serial, "27:9f:8b:45:2b:92:ad:a6:53:fd:79:74:5e:5a:bd:10:e3:34:54:5e");
        assert_eq!(leaf.subject_alternative_names, vec![
            "DNS:tls-test.nzyme.local".to_string(),
            "DNS:alt.nzyme.local".to_string(),
            "IP:10.0.0.1".to_string()
        ]);
        assert_eq!(leaf.not_before.unwrap().format("%Y-%m-%d").to_string(), "2026-10-16");
        assert_eq!(leaf.not_after.unwrap().format("%Y-%m-%d").to_string(), "2126-09-22");
        assert!(leaf.public_key_algorithm.starts_with("EC "), "{}", leaf.public_key_algorithm);
        assert_eq!(leaf.public_key_bits, 256);
        assert_eq!(leaf.signature_algorithm, "ecdsa-with-SHA256");
    }

    #[test]
    fn parses_ca_certificate() {
        let ca = TlsCertificate::from_der(CA_DER);
        assert!(ca.parsed);
        assert_eq!(ca.fingerprint_sha256, CA_SHA256);
        assert_eq!(ca.subject, CA_SUBJECT);
        assert_eq!(ca.issuer, CA_SUBJECT);
        assert!(ca.self_signed);
        assert!(ca.is_ca);
        assert!(ca.subject_alternative_names.is_empty());
    }

    #[test]
    fn garbage_still_yields_fingerprint() {
        let cert = TlsCertificate::from_der(&[0xFF; 20]);
        assert!(!cert.parsed);
        assert_eq!(cert.fingerprint_sha256.len(), 64);
        assert_eq!(cert.der_length, 20);
        assert!(cert.subject.is_empty());
    }
}
