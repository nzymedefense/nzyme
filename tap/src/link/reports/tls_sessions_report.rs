use std::collections::HashMap;
use std::sync::MutexGuard;
use chrono::{DateTime, Utc};
use serde::Serialize;
use serde_with::{serde_as, base64::Base64};
use crate::protocols::detection::taggers::network_protocols::tls_tagger::{ClientHello, ServerHello, TlsAlert};
use crate::protocols::parsers::l4_key::L4Key;
use crate::wired::ethernet::types::tls_session::TlsSession;
use crate::wired::tls::certificate::TlsCertificate;

/*
 * Certificates are shipped once per report as raw DER, keyed by their SHA-256 fingerprint.
 * Sessions reference them by fingerprint. The node parses and stores them in its shared
 * certificates table.
 */
#[serde_as]
#[derive(Serialize)]
pub struct TlsSessionsReport {
    pub sessions: Vec<TlsSessionReport>,
    #[serde_as(as = "HashMap<_, Base64>")]
    pub certificates: HashMap<String, Vec<u8>>
}

#[derive(Serialize)]
pub struct TlsSessionReport {
    pub connection_status: String,
    pub source_mac: Option<String>,
    pub destination_mac: Option<String>,
    pub source_address: String,
    pub source_port: u16,
    pub destination_address: String,
    pub destination_port: u16,
    pub bytes_count: u64,
    pub established_at: DateTime<Utc>,
    pub terminated_at: Option<DateTime<Utc>>,
    pub most_recent_segment_time: DateTime<Utc>,
    pub client_hello: Option<TlsClientHelloReport>,
    pub server_hello: Option<TlsServerHelloReport>,
    pub hello_retry_request: bool,
    // Leaf first. SHA-256 fingerprints, hex. Keys into the report's `certificates`.
    pub server_chain_sha256: Vec<String>,
    pub client_chain_sha256: Vec<String>,
    pub client_certificate_requested: bool,
    pub alerts: Vec<TlsAlertReport>,
    pub client_change_cipher_spec: bool,
    pub server_change_cipher_spec: bool,
    pub client_application_data_records: u64,
    pub client_application_data_bytes: u64,
    pub server_application_data_records: u64,
    pub server_application_data_bytes: u64
}

#[derive(Serialize)]
pub struct TlsClientHelloReport {
    pub record_version: String,
    pub legacy_version: String,
    pub supported_versions: Vec<String>,
    pub server_name: Option<String>,
    pub cipher_suites: Vec<u16>,
    pub extensions: Vec<u16>,
    pub supported_groups: Vec<u16>,
    pub ec_point_formats: Vec<u8>,
    pub signature_algorithms: Vec<u16>,
    pub alpn: Vec<String>,
    pub session_id_present: bool,
    pub offers_session_ticket: bool,
    pub offers_pre_shared_key: bool,
    pub encrypted_client_hello: bool
}

#[derive(Serialize)]
pub struct TlsServerHelloReport {
    pub record_version: String,
    pub legacy_version: String,
    pub version: String,
    pub cipher_suite: u16,
    pub compression_method: u8,
    pub extensions: Vec<u16>,
    pub alpn: Option<String>,
    pub session_resumed: bool
}

#[derive(Serialize)]
pub struct TlsAlertReport {
    pub direction: String,
    pub level: u8,
    pub description: u8,
    pub description_name: String
}

pub fn generate(s: &MutexGuard<HashMap<L4Key, TlsSession>>) -> TlsSessionsReport {
    let mut certificates: HashMap<String, Vec<u8>> = HashMap::new();
    let mut sessions = Vec::with_capacity(s.len());

    for session in s.values() {
        let hs = &session.handshake;
        for cert in hs.server_certificates.iter().chain(hs.client_certificates.iter()) {
            certificates.entry(cert.fingerprint_sha256.clone()).or_insert_with(|| cert.der.clone());
        }

        sessions.push(session_report(session));
    }

    TlsSessionsReport { sessions, certificates }
}

fn session_report(session: &TlsSession) -> TlsSessionReport {
    let hs = &session.handshake;

    TlsSessionReport {
        connection_status: session.connection_status.to_string(),
        source_mac: session.source_mac.clone(),
        destination_mac: session.destination_mac.clone(),
        source_address: session.source_address.to_string(),
        source_port: session.source_port,
        destination_address: session.destination_address.to_string(),
        destination_port: session.destination_port,
        bytes_count: session.bytes_count,
        established_at: session.established_at,
        terminated_at: session.terminated_at,
        most_recent_segment_time: session.most_recent_segment_time,
        client_hello: hs.client_hello.as_ref().map(client_hello_report),
        server_hello: hs.server_hello.as_ref().map(server_hello_report),
        hello_retry_request: hs.hello_retry_request,
        server_chain_sha256: chain_fingerprints(&hs.server_certificates),
        client_chain_sha256: chain_fingerprints(&hs.client_certificates),
        client_certificate_requested: hs.client_certificate_requested,
        alerts: hs.alerts.iter().map(alert_report).collect(),
        client_change_cipher_spec: hs.client_change_cipher_spec,
        server_change_cipher_spec: hs.server_change_cipher_spec,
        client_application_data_records: hs.client_application_data_records as u64,
        client_application_data_bytes: hs.client_application_data_bytes as u64,
        server_application_data_records: hs.server_application_data_records as u64,
        server_application_data_bytes: hs.server_application_data_bytes as u64
    }
}

fn chain_fingerprints(chain: &[TlsCertificate]) -> Vec<String> {
    chain.iter().map(|c| c.fingerprint_sha256.clone()).collect()
}

fn client_hello_report(ch: &ClientHello) -> TlsClientHelloReport {
    TlsClientHelloReport {
        record_version: ch.record_version.to_string(),
        legacy_version: ch.legacy_version.to_string(),
        supported_versions: ch.supported_versions.iter().map(|v| v.to_string()).collect(),
        server_name: ch.server_name.clone(),
        cipher_suites: ch.cipher_suites.clone(),
        extensions: ch.extensions.clone(),
        supported_groups: ch.supported_groups.clone(),
        ec_point_formats: ch.ec_point_formats.clone(),
        signature_algorithms: ch.signature_algorithms.clone(),
        alpn: ch.alpn.clone(),
        session_id_present: ch.session_id_present,
        offers_session_ticket: ch.offers_session_ticket,
        offers_pre_shared_key: ch.offers_pre_shared_key,
        encrypted_client_hello: ch.encrypted_client_hello
    }
}

fn server_hello_report(sh: &ServerHello) -> TlsServerHelloReport {
    TlsServerHelloReport {
        record_version: sh.record_version.to_string(),
        legacy_version: sh.legacy_version.to_string(),
        version: sh.version.to_string(),
        cipher_suite: sh.cipher_suite,
        compression_method: sh.compression_method,
        extensions: sh.extensions.clone(),
        alpn: sh.alpn.clone(),
        session_resumed: sh.session_resumed
    }
}

fn alert_report(alert: &TlsAlert) -> TlsAlertReport {
    TlsAlertReport {
        direction: format!("{:?}", alert.direction),
        level: alert.level,
        description: alert.description,
        description_name: alert.description_name.to_string()
    }
}

#[cfg(test)]
mod tests {
    use base64::Engine;
    use serde_json::Value;
    use crate::wired::ethernet::types::tls_session::tests::test_tls_session;
    use crate::wired::tls::certificate::testdata::{CA_DER, CA_SHA256, LEAF_DER, LEAF_SHA256};
    use super::*;

    fn report_json() -> Value {
        let session = test_tls_session();
        let mut sessions = HashMap::new();
        sessions.insert(session.tcp_session_key.clone(), session);
        let sessions = std::sync::Mutex::new(sessions);
        let guard = sessions.lock().unwrap();

        serde_json::to_value(generate(&guard)).unwrap()
    }

    #[test]
    fn serializes_report() {
        let json = report_json();
        let session = &json["sessions"][0];

        assert_eq!(session["connection_status"], "Inactive");
        assert_eq!(session["source_address"], "10.0.0.10");
        assert_eq!(session["destination_port"], 443);
        assert_eq!(session["bytes_count"], 5500);
        assert_eq!(session["established_at"], "2026-10-16T12:00:00Z");
        assert_eq!(session["terminated_at"], "2026-10-16T12:00:05Z");
        assert_eq!(session["client_hello"]["server_name"], "example.com");
        assert_eq!(session["client_hello"]["supported_versions"][0], "TLSv1.3");
        assert_eq!(session["client_hello"]["cipher_suites"][0], 0x1301);
        assert_eq!(session["server_hello"]["version"], "TLSv1.2");
        assert_eq!(session["server_hello"]["cipher_suite"], 0xC02F);
        assert_eq!(session["server_hello"]["alpn"], "h2");
        assert_eq!(session["server_chain_sha256"][0], LEAF_SHA256);
        assert_eq!(session["server_chain_sha256"][1], CA_SHA256);
        assert_eq!(session["client_chain_sha256"].as_array().unwrap().len(), 0);
        assert_eq!(session["alerts"][0]["direction"], "ServerToClient");
        assert_eq!(session["alerts"][0]["description_name"], "close_notify");
        assert_eq!(session["server_application_data_bytes"], 3900);

        // Certificates shipped once, as base64 DER, keyed by fingerprint.
        let certificates = json["certificates"].as_object().unwrap();
        assert_eq!(certificates.len(), 2);
        let decode = |k: &str| base64::engine::general_purpose::STANDARD
            .decode(certificates[k].as_str().unwrap()).unwrap();
        assert_eq!(decode(LEAF_SHA256), LEAF_DER);
        assert_eq!(decode(CA_SHA256), CA_DER);

        // Fixture for the node-side deserialization test.
        println!("REPORT_FIXTURE {}", json);
    }

    #[test]
    fn deduplicates_certificates_across_sessions() {
        let a = test_tls_session();
        let mut b = test_tls_session();
        b.source_port = 51001;
        b.tcp_session_key = L4Key::new(b.source_address, b.source_port, b.destination_address, b.destination_port);

        let mut sessions = HashMap::new();
        sessions.insert(a.tcp_session_key.clone(), a);
        sessions.insert(b.tcp_session_key.clone(), b);
        let sessions = std::sync::Mutex::new(sessions);
        let report = generate(&sessions.lock().unwrap());

        assert_eq!(report.sessions.len(), 2);
        assert_eq!(report.certificates.len(), 2);
    }
}
