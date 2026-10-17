use crate::wired::tls::certificate::TlsCertificate;
use crate::wired::tls::version::TlsVersion;

const RECORD_TYPE_CHANGE_CIPHER_SPEC: u8 = 0x14;
const RECORD_TYPE_ALERT: u8 = 0x15;
const RECORD_TYPE_HANDSHAKE: u8 = 0x16;
const RECORD_TYPE_APPLICATION_DATA: u8 = 0x17;
const RECORD_VERSION_MAJOR: u8 = 0x03;
const RECORD_VERSION_MINOR_MAX: u8 = 0x04;

const HANDSHAKE_CLIENT_HELLO: u8 = 0x01;
const HANDSHAKE_SERVER_HELLO: u8 = 0x02;
const HANDSHAKE_CERTIFICATE: u8 = 0x0B;
const HANDSHAKE_CERTIFICATE_REQUEST: u8 = 0x0D;

const EXT_SERVER_NAME: u16 = 0x0000;
const EXT_SUPPORTED_GROUPS: u16 = 0x000A;
const EXT_EC_POINT_FORMATS: u16 = 0x000B;
const EXT_SIGNATURE_ALGORITHMS: u16 = 0x000D;
const EXT_ALPN: u16 = 0x0010;
const EXT_SESSION_TICKET: u16 = 0x0023;
const EXT_PRE_SHARED_KEY: u16 = 0x0029;
const EXT_SUPPORTED_VERSIONS: u16 = 0x002B;
const EXT_ENCRYPTED_CLIENT_HELLO: u16 = 0xFE0D;

const SNI_TYPE_HOST_NAME: u8 = 0x00;

const MAX_RECORD_LENGTH: usize = 16384;
const RECORD_HEADER_LENGTH: usize = 5;
const HANDSHAKE_HEADER_LENGTH: usize = 4;

// Sanity caps to bound the work and log volume on hostile input.
const MAX_CERTIFICATES_PER_CHAIN: usize = 16;
const MAX_ALERTS: usize = 32;

// TLS 1.3 HelloRetryRequest is a ServerHello with this fixed random. (RFC 8446, 4.1.3)
const HELLO_RETRY_REQUEST_RANDOM: [u8; 32] = [
    0xCF, 0x21, 0xAD, 0x74, 0xE5, 0x9A, 0x61, 0x11, 0xBE, 0x1D, 0x8C, 0x02, 0x1E, 0x65, 0xB8, 0x91,
    0xC2, 0xA2, 0x11, 0x16, 0x7A, 0xBB, 0x8C, 0x5E, 0x07, 0x9E, 0x09, 0xE2, 0xC8, 0xA8, 0x33, 0x9C
];

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub enum TlsDirection {
    ClientToServer,
    ServerToClient
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct TlsAlert {
    pub direction: TlsDirection,
    pub level: u8,
    pub description: u8,
    pub description_name: &'static str
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ClientHello {
    pub record_version: TlsVersion,
    pub legacy_version: TlsVersion,
    pub supported_versions: Vec<TlsVersion>,
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

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct ServerHello {
    pub record_version: TlsVersion,
    pub legacy_version: TlsVersion,
    pub version: TlsVersion,
    pub cipher_suite: u16,
    pub compression_method: u8,
    pub extensions: Vec<u16>,
    pub alpn: Option<String>,
    pub session_resumed: bool
}

#[derive(Debug, Clone, PartialEq, Eq, Default)]
pub struct TlsHandshake {
    pub client_hello: Option<ClientHello>,
    pub server_hello: Option<ServerHello>,
    pub hello_retry_request: bool,
    pub server_certificates: Vec<TlsCertificate>,
    pub client_certificates: Vec<TlsCertificate>,
    pub client_certificate_requested: bool,
    pub alerts: Vec<TlsAlert>,
    pub client_change_cipher_spec: bool,
    pub server_change_cipher_spec: bool,
    /*
     * Application data records. On TLS 1.3 the encrypted handshake messages following the
     * ServerHello are indistinguishable from application data and counted here as well.
     */
    pub client_application_data_records: usize,
    pub client_application_data_bytes: usize,
    pub server_application_data_records: usize,
    pub server_application_data_bytes: usize
}

pub fn tag(cts: &[u8], stc: &[u8]) -> Option<TlsHandshake> {
    if !is_record_header(cts, &[RECORD_TYPE_HANDSHAKE])
        || cts.get(RECORD_HEADER_LENGTH) != Some(&HANDSHAKE_CLIENT_HELLO) {
        return None;
    }

    if !stc.is_empty() && !is_record_header(stc, &[RECORD_TYPE_HANDSHAKE, RECORD_TYPE_ALERT]) {
        return None;
    }

    let client = parse_direction(cts, TlsDirection::ClientToServer);
    let server = parse_direction(stc, TlsDirection::ServerToClient);

    let mut handshake = TlsHandshake {
        client_change_cipher_spec: client.change_cipher_spec,
        server_change_cipher_spec: server.change_cipher_spec,
        client_application_data_records: client.application_data_records,
        client_application_data_bytes: client.application_data_bytes,
        server_application_data_records: server.application_data_records,
        server_application_data_bytes: server.application_data_bytes,
        ..Default::default()
    };

    let record_version = |buf: &[u8]| TlsVersion::from_u16(u16::from_be_bytes([buf[1], buf[2]]));

    let mut client_session_id: Vec<u8> = vec![];
    for (i, (message_type, body)) in client.messages.iter().enumerate() {
        match *message_type {
            HANDSHAKE_CLIENT_HELLO => {
                match parse_client_hello(body, record_version(cts)) {
                    Some((hello, session_id)) => {
                        handshake.client_hello = Some(hello);
                        client_session_id = session_id;
                    },
                    // A complete first ClientHello that does not parse is not TLS.
                    None if i == 0 => return None,
                    None => {}
                }
            },
            HANDSHAKE_CERTIFICATE => handshake.client_certificates = parse_certificates(body),
            _ => {}
        }
    }

    for (message_type, body) in &server.messages {
        match *message_type {
            HANDSHAKE_SERVER_HELLO => {
                if let Some((hello, random, session_id)) = parse_server_hello(body, record_version(stc)) {
                    handshake.hello_retry_request = random == HELLO_RETRY_REQUEST_RANDOM;

                    let mut hello = hello;
                    hello.session_resumed = hello.version != TlsVersion::Tls13
                        && !session_id.is_empty()
                        && session_id == client_session_id;
                    handshake.server_hello = Some(hello);
                }
            },
            HANDSHAKE_CERTIFICATE => handshake.server_certificates = parse_certificates(body),
            HANDSHAKE_CERTIFICATE_REQUEST => handshake.client_certificate_requested = true,
            _ => {}
        }
    }

    handshake.alerts.extend(client.alerts);
    handshake.alerts.extend(server.alerts);
    handshake.alerts.truncate(MAX_ALERTS);

    Some(handshake)
}

fn is_record_header(buf: &[u8], allowed_types: &[u8]) -> bool {
    if buf.len() < RECORD_HEADER_LENGTH {
        return false;
    }

    let length = u16::from_be_bytes([buf[3], buf[4]]) as usize;

    allowed_types.contains(&buf[0])
        && buf[1] == RECORD_VERSION_MAJOR
        && buf[2] <= RECORD_VERSION_MINOR_MAX
        && length > 0
        && length <= MAX_RECORD_LENGTH
}

#[derive(Default)]
struct DirectionParse {
    messages: Vec<(u8, Vec<u8>)>,
    alerts: Vec<TlsAlert>,
    change_cipher_spec: bool,
    application_data_records: usize,
    application_data_bytes: usize
}

fn parse_direction(buf: &[u8], direction: TlsDirection) -> DirectionParse {
    let all_types = [
        RECORD_TYPE_CHANGE_CIPHER_SPEC,
        RECORD_TYPE_ALERT,
        RECORD_TYPE_HANDSHAKE,
        RECORD_TYPE_APPLICATION_DATA
    ];

    let hello_type = match direction {
        TlsDirection::ClientToServer => HANDSHAKE_CLIENT_HELLO,
        TlsDirection::ServerToClient => HANDSHAKE_SERVER_HELLO
    };

    let mut result = DirectionParse::default();
    let mut handshake_fragments: Vec<u8> = vec![];
    let mut pos = 0;

    while is_record_header(&buf[pos..], &all_types) {
        let content_type = buf[pos];
        let length = u16::from_be_bytes([buf[pos + 3], buf[pos + 4]]) as usize;
        let body_start = pos + RECORD_HEADER_LENGTH;
        let body_end = body_start + length;

        if body_end > buf.len() {
            // Incomplete record. More data may arrive.
            break;
        }

        let body = &buf[body_start..body_end];
        pos = body_end;

        match content_type {
            RECORD_TYPE_HANDSHAKE => {
                /*
                 * After a ChangeCipherSpec, handshake records are encrypted (TLS 1.2 Finished)
                 * and cannot be parsed. The exception is the TLS 1.3 HelloRetryRequest flow,
                 * where both sides send a second hello after their compatibility-mode CCS.
                 */
                if result.change_cipher_spec && body.first() != Some(&hello_type) {
                    continue;
                }

                handshake_fragments.extend_from_slice(body);
                drain_handshake_messages(&mut handshake_fragments, &mut result.messages);
            },
            RECORD_TYPE_ALERT => {
                if body.len() >= 2 && result.alerts.len() < MAX_ALERTS {
                    result.alerts.push(TlsAlert {
                        direction,
                        level: body[0],
                        description: body[1],
                        description_name: alert_description_name(body[1])
                    });
                }
            },
            RECORD_TYPE_CHANGE_CIPHER_SPEC => {
                result.change_cipher_spec = true;
                handshake_fragments.clear();
            },
            RECORD_TYPE_APPLICATION_DATA => {
                result.application_data_records += 1;
                result.application_data_bytes += body.len();
            },
            _ => unreachable!()
        }
    }

    result
}

fn drain_handshake_messages(fragments: &mut Vec<u8>, messages: &mut Vec<(u8, Vec<u8>)>) {
    loop {
        if fragments.len() < HANDSHAKE_HEADER_LENGTH {
            return;
        }

        let message_type = fragments[0];
        let length = u32::from_be_bytes([0, fragments[1], fragments[2], fragments[3]]) as usize;
        let end = HANDSHAKE_HEADER_LENGTH + length;

        if fragments.len() < end {
            // Incomplete message. Keep the fragment, more data may arrive.
            return;
        }

        messages.push((message_type, fragments[HANDSHAKE_HEADER_LENGTH..end].to_vec()));
        fragments.drain(..end);
    }
}

struct Cursor<'a> {
    data: &'a [u8],
    pos: usize
}

impl<'a> Cursor<'a> {
    fn new(data: &'a [u8]) -> Self {
        Self { data, pos: 0 }
    }

    fn is_empty(&self) -> bool {
        self.pos >= self.data.len()
    }

    fn take(&mut self, n: usize) -> Option<&'a [u8]> {
        let end = self.pos.checked_add(n)?;
        let slice = self.data.get(self.pos..end)?;
        self.pos = end;
        Some(slice)
    }

    fn u8(&mut self) -> Option<u8> {
        self.take(1).map(|b| b[0])
    }

    fn u16(&mut self) -> Option<u16> {
        self.take(2).map(|b| u16::from_be_bytes([b[0], b[1]]))
    }

    fn u24(&mut self) -> Option<usize> {
        self.take(3).map(|b| u32::from_be_bytes([0, b[0], b[1], b[2]]) as usize)
    }

    fn vec_u8(&mut self) -> Option<&'a [u8]> {
        let len = self.u8()? as usize;
        self.take(len)
    }

    fn vec_u16(&mut self) -> Option<&'a [u8]> {
        let len = self.u16()? as usize;
        self.take(len)
    }

    fn vec_u24(&mut self) -> Option<&'a [u8]> {
        let len = self.u24()?;
        self.take(len)
    }
}

fn u16_list(data: &[u8]) -> Vec<u16> {
    data.chunks_exact(2).map(|c| u16::from_be_bytes([c[0], c[1]])).collect()
}

fn parse_extensions(data: &[u8]) -> Vec<(u16, &[u8])> {
    let mut cursor = Cursor::new(data);
    let mut extensions = vec![];

    while !cursor.is_empty() {
        let (Some(ext_type), Some(ext_data)) = (cursor.u16(), cursor.vec_u16()) else {
            break;
        };
        extensions.push((ext_type, ext_data));
    }

    extensions
}

fn is_grease(v: u16) -> bool {
    (v & 0x0F0F) == 0x0A0A && (v >> 8) == (v & 0xFF)
}

fn without_grease(values: &[u16]) -> Vec<u16> {
    values.iter().copied().filter(|v| !is_grease(*v)).collect()
}

fn parse_client_hello(body: &[u8], record_version: TlsVersion) -> Option<(ClientHello, Vec<u8>)> {
    let mut cursor = Cursor::new(body);

    let legacy_version = TlsVersion::from_u16(cursor.u16()?);
    cursor.take(32)?; // Random.
    let session_id = cursor.vec_u8()?.to_vec();
    let cipher_suites = without_grease(&u16_list(cursor.vec_u16()?));
    cursor.vec_u8()?; // Compression methods.

    // Extensions are optional on old clients.
    let extensions = if cursor.is_empty() { vec![] } else { parse_extensions(cursor.vec_u16()?) };

    let mut hello = ClientHello {
        record_version,
        legacy_version,
        supported_versions: vec![],
        server_name: None,
        cipher_suites,
        extensions: vec![],
        supported_groups: vec![],
        ec_point_formats: vec![],
        signature_algorithms: vec![],
        alpn: vec![],
        session_id_present: !session_id.is_empty(),
        offers_session_ticket: false,
        offers_pre_shared_key: false,
        encrypted_client_hello: false
    };

    for (ext_type, ext_data) in extensions {
        if is_grease(ext_type) {
            continue;
        }

        hello.extensions.push(ext_type);

        match ext_type {
            EXT_SERVER_NAME => hello.server_name = parse_server_name(ext_data),
            EXT_SUPPORTED_GROUPS => {
                if let Some(list) = Cursor::new(ext_data).vec_u16() {
                    hello.supported_groups = without_grease(&u16_list(list));
                }
            },
            EXT_EC_POINT_FORMATS => {
                if let Some(list) = Cursor::new(ext_data).vec_u8() {
                    hello.ec_point_formats = list.to_vec();
                }
            },
            EXT_SIGNATURE_ALGORITHMS => {
                if let Some(list) = Cursor::new(ext_data).vec_u16() {
                    hello.signature_algorithms = u16_list(list);
                }
            },
            EXT_ALPN => hello.alpn = parse_alpn(ext_data),
            EXT_SUPPORTED_VERSIONS => {
                if let Some(list) = Cursor::new(ext_data).vec_u8() {
                    hello.supported_versions = without_grease(&u16_list(list))
                        .into_iter()
                        .map(TlsVersion::from_u16)
                        .collect();
                }
            },
            EXT_SESSION_TICKET => hello.offers_session_ticket = true,
            EXT_PRE_SHARED_KEY => hello.offers_pre_shared_key = true,
            EXT_ENCRYPTED_CLIENT_HELLO => hello.encrypted_client_hello = true,
            _ => {}
        }
    }

    Some((hello, session_id))
}

fn parse_server_hello(body: &[u8], record_version: TlsVersion) -> Option<(ServerHello, [u8; 32], Vec<u8>)> {
    let mut cursor = Cursor::new(body);

    let legacy_version = TlsVersion::from_u16(cursor.u16()?);
    let mut random = [0u8; 32];
    random.copy_from_slice(cursor.take(32)?);
    let session_id = cursor.vec_u8()?.to_vec();
    let cipher_suite = cursor.u16()?;
    let compression_method = cursor.u8()?;

    let extensions = if cursor.is_empty() { vec![] } else { parse_extensions(cursor.vec_u16()?) };

    let mut hello = ServerHello {
        record_version,
        legacy_version,
        version: legacy_version,
        cipher_suite,
        compression_method,
        extensions: vec![],
        alpn: None,
        session_resumed: false
    };

    for (ext_type, ext_data) in extensions {
        if is_grease(ext_type) {
            continue;
        }

        hello.extensions.push(ext_type);

        match ext_type {
            EXT_SUPPORTED_VERSIONS => {
                if let Some(v) = Cursor::new(ext_data).u16() {
                    hello.version = TlsVersion::from_u16(v);
                }
            },
            EXT_ALPN => hello.alpn = parse_alpn(ext_data).into_iter().next(),
            _ => {}
        }
    }

    Some((hello, random, session_id))
}

fn parse_server_name(data: &[u8]) -> Option<String> {
    let mut list = Cursor::new(Cursor::new(data).vec_u16()?);

    while !list.is_empty() {
        let name_type = list.u8()?;
        let name = list.vec_u16()?;

        if name_type == SNI_TYPE_HOST_NAME {
            return Some(String::from_utf8_lossy(name).into_owned());
        }
    }

    None
}

fn parse_alpn(data: &[u8]) -> Vec<String> {
    let mut protocols = vec![];

    if let Some(list) = Cursor::new(data).vec_u16() {
        let mut list = Cursor::new(list);
        while let Some(protocol) = list.vec_u8() {
            protocols.push(String::from_utf8_lossy(protocol).into_owned());
        }
    }

    protocols
}

fn parse_certificates(body: &[u8]) -> Vec<TlsCertificate> {
    let mut certificates = vec![];

    let Some(chain) = Cursor::new(body).vec_u24() else {
        return certificates;
    };

    let mut chain = Cursor::new(chain);
    while let Some(der) = chain.vec_u24() {
        if certificates.len() >= MAX_CERTIFICATES_PER_CHAIN {
            break;
        }

        certificates.push(TlsCertificate::from_der(der));
    }

    certificates
}

fn alert_description_name(description: u8) -> &'static str {
    match description {
        0 => "close_notify",
        10 => "unexpected_message",
        20 => "bad_record_mac",
        21 => "decryption_failed",
        22 => "record_overflow",
        30 => "decompression_failure",
        40 => "handshake_failure",
        41 => "no_certificate",
        42 => "bad_certificate",
        43 => "unsupported_certificate",
        44 => "certificate_revoked",
        45 => "certificate_expired",
        46 => "certificate_unknown",
        47 => "illegal_parameter",
        48 => "unknown_ca",
        49 => "access_denied",
        50 => "decode_error",
        51 => "decrypt_error",
        60 => "export_restriction",
        70 => "protocol_version",
        71 => "insufficient_security",
        80 => "internal_error",
        86 => "inappropriate_fallback",
        90 => "user_canceled",
        100 => "no_renegotiation",
        109 => "missing_extension",
        110 => "unsupported_extension",
        111 => "certificate_unobtainable",
        112 => "unrecognized_name",
        113 => "bad_certificate_status_response",
        114 => "bad_certificate_hash_value",
        115 => "unknown_psk_identity",
        116 => "certificate_required",
        120 => "no_application_protocol",
        _ => "unknown"
    }
}

#[cfg(test)]
mod tests {
    use std::sync::Arc;
    use rustls::client::danger::{HandshakeSignatureValid, ServerCertVerified, ServerCertVerifier};
    use rustls::crypto::{ring, CryptoProvider};
    use rustls::pki_types::{CertificateDer, PrivateKeyDer, PrivatePkcs8KeyDer, ServerName, UnixTime};
    use rustls::server::WebPkiClientVerifier;
    use rustls::{ClientConfig, ClientConnection, DigitallySignedStruct, RootCertStore, ServerConfig, ServerConnection, SignatureScheme, SupportedProtocolVersion};
    use super::*;

    use crate::wired::tls::certificate::testdata::{
        CA_DER as TEST_CA_DER, CA_SHA256 as TEST_CA_SHA256, CA_SUBJECT as TEST_CA_SUBJECT,
        LEAF_DER as TEST_LEAF_DER, LEAF_KEY_DER as TEST_LEAF_KEY_DER,
        LEAF_SHA256 as TEST_LEAF_SHA256, LEAF_SUBJECT as TEST_LEAF_SUBJECT,
        SERVER_NAME as TEST_SERVER_NAME
    };

    // TLS 1.2 record carrying a (truncated) ClientHello.
    const CLIENT_HELLO: &[u8] = &[0x16, 0x03, 0x01, 0x00, 0xA5, 0x01, 0x00, 0x00, 0xA1, 0x03, 0x03];
    const SERVER_HELLO: &[u8] = &[0x16, 0x03, 0x03, 0x00, 0x5A, 0x02, 0x00, 0x00, 0x56, 0x03, 0x03];
    const ALERT: &[u8] = &[0x15, 0x03, 0x03, 0x00, 0x02, 0x02, 0x28];

    #[derive(Debug)]
    struct AcceptAnything;

    impl ServerCertVerifier for AcceptAnything {
        fn verify_server_cert(&self, _: &CertificateDer<'_>, _: &[CertificateDer<'_>],
                              _: &ServerName<'_>, _: &[u8], _: UnixTime)
            -> Result<ServerCertVerified, rustls::Error> {
            Ok(ServerCertVerified::assertion())
        }

        fn verify_tls12_signature(&self, _: &[u8], _: &CertificateDer<'_>, _: &DigitallySignedStruct)
            -> Result<HandshakeSignatureValid, rustls::Error> {
            Ok(HandshakeSignatureValid::assertion())
        }

        fn verify_tls13_signature(&self, _: &[u8], _: &CertificateDer<'_>, _: &DigitallySignedStruct)
            -> Result<HandshakeSignatureValid, rustls::Error> {
            Ok(HandshakeSignatureValid::assertion())
        }

        fn supported_verify_schemes(&self) -> Vec<SignatureScheme> {
            vec![SignatureScheme::ECDSA_NISTP256_SHA256, SignatureScheme::RSA_PSS_SHA256]
        }
    }

    fn cert_chain() -> Vec<CertificateDer<'static>> {
        vec![CertificateDer::from(TEST_LEAF_DER.to_vec()), CertificateDer::from(TEST_CA_DER.to_vec())]
    }

    fn private_key() -> PrivateKeyDer<'static> {
        PrivateKeyDer::Pkcs8(PrivatePkcs8KeyDer::from(TEST_LEAF_KEY_DER.to_vec()))
    }

    fn client_config(provider: CryptoProvider,
                     versions: &[&'static SupportedProtocolVersion],
                     verify: bool,
                     client_auth: bool) -> ClientConfig {
        let builder = ClientConfig::builder_with_provider(Arc::new(provider))
            .with_protocol_versions(versions)
            .unwrap();

        let builder = if verify {
            // Empty root store: The server certificate is unknown and must be rejected.
            builder.with_root_certificates(RootCertStore::empty())
        } else {
            builder.dangerous().with_custom_certificate_verifier(Arc::new(AcceptAnything))
        };

        let mut config = if client_auth {
            builder.with_client_auth_cert(cert_chain(), private_key()).unwrap()
        } else {
            builder.with_no_client_auth()
        };

        config.alpn_protocols = vec![b"h2".to_vec(), b"http/1.1".to_vec()];
        config
    }

    fn server_config(provider: CryptoProvider,
                     versions: &[&'static SupportedProtocolVersion],
                     client_auth: bool) -> ServerConfig {
        let provider = Arc::new(provider);
        let builder = ServerConfig::builder_with_provider(provider.clone())
            .with_protocol_versions(versions)
            .unwrap();

        let builder = if client_auth {
            let mut roots = RootCertStore::empty();
            roots.add(CertificateDer::from(TEST_CA_DER.to_vec())).unwrap();
            let verifier = WebPkiClientVerifier::builder_with_provider(Arc::new(roots), provider)
                .build()
                .unwrap();
            builder.with_client_cert_verifier(verifier)
        } else {
            builder.with_no_client_auth()
        };

        let mut config = builder.with_single_cert(cert_chain(), private_key()).unwrap();
        config.alpn_protocols = vec![b"h2".to_vec()];
        config
    }

    fn run_handshake(client_config: ClientConfig, server_config: ServerConfig) -> (Vec<u8>, Vec<u8>) {
        let server_name = ServerName::try_from(TEST_SERVER_NAME).unwrap();
        let mut client = ClientConnection::new(Arc::new(client_config), server_name).unwrap();
        let mut server = ServerConnection::new(Arc::new(server_config)).unwrap();

        let mut cts: Vec<u8> = vec![];
        let mut stc: Vec<u8> = vec![];

        // Application data gets queued and sent once the handshake completes.
        use std::io::Write;
        client.writer().write_all(b"GET / HTTP/1.1\r\n\r\n").unwrap();
        server.writer().write_all(b"HTTP/1.1 200 OK\r\n\r\n").unwrap();

        for _ in 0..32 {
            let mut progressed = false;

            while client.wants_write() {
                let mut buf = vec![];
                client.write_tls(&mut buf).unwrap();
                cts.extend_from_slice(&buf);
                let mut slice = buf.as_slice();
                server.read_tls(&mut slice).unwrap();
                let _ = server.process_new_packets(); // Errors surface as alerts on the wire.
                progressed = true;
            }

            while server.wants_write() {
                let mut buf = vec![];
                server.write_tls(&mut buf).unwrap();
                stc.extend_from_slice(&buf);
                let mut slice = buf.as_slice();
                client.read_tls(&mut slice).unwrap();
                let _ = client.process_new_packets();
                progressed = true;
            }

            if !progressed {
                break;
            }
        }

        (cts, stc)
    }

    fn default_handshake(versions: &[&'static SupportedProtocolVersion]) -> (Vec<u8>, Vec<u8>) {
        run_handshake(
            client_config(ring::default_provider(), versions, false, false),
            server_config(ring::default_provider(), versions, false)
        )
    }

    #[test]
    fn parses_tls13_handshake() {
        let (cts, stc) = default_handshake(&[&rustls::version::TLS13]);
        let tls = tag(&cts, &stc).unwrap();

        let ch = tls.client_hello.as_ref().unwrap();
        assert_eq!(ch.legacy_version, TlsVersion::Tls12);
        assert_eq!(ch.supported_versions, vec![TlsVersion::Tls13]);
        assert_eq!(ch.server_name.as_deref(), Some(TEST_SERVER_NAME));
        assert_eq!(ch.alpn, vec!["h2".to_string(), "http/1.1".to_string()]);
        assert!(!ch.cipher_suites.is_empty());
        assert!(ch.cipher_suites.contains(&0x1301)); // TLS_AES_128_GCM_SHA256
        assert!(ch.extensions.contains(&EXT_SERVER_NAME));
        assert!(ch.extensions.contains(&EXT_SUPPORTED_VERSIONS));
        assert!(!ch.supported_groups.is_empty());
        assert!(!ch.signature_algorithms.is_empty());
        assert!(ch.session_id_present); // TLS 1.3 compatibility mode.
        assert!(!ch.offers_pre_shared_key);
        assert!(!ch.encrypted_client_hello);

        let sh = tls.server_hello.as_ref().unwrap();
        assert_eq!(sh.legacy_version, TlsVersion::Tls12);
        assert_eq!(sh.version, TlsVersion::Tls13);
        assert!(sh.cipher_suite == 0x1301 || sh.cipher_suite == 0x1302 || sh.cipher_suite == 0x1303);
        assert_eq!(sh.compression_method, 0);
        assert!(sh.extensions.contains(&EXT_SUPPORTED_VERSIONS));
        assert!(sh.alpn.is_none()); // Encrypted in TLS 1.3.
        assert!(!sh.session_resumed);

        assert!(!tls.hello_retry_request);
        assert!(tls.server_certificates.is_empty()); // Encrypted in TLS 1.3.
        assert!(tls.client_certificates.is_empty());
        assert!(!tls.client_certificate_requested);
        assert!(tls.alerts.is_empty());
        assert!(tls.client_change_cipher_spec);
        assert!(tls.server_change_cipher_spec);
        assert!(tls.client_application_data_records > 0);
        assert!(tls.server_application_data_records > 0);
        assert!(tls.client_application_data_bytes > 0);
        assert!(tls.server_application_data_bytes > 0);
    }

    #[test]
    fn parses_tls12_handshake_with_certificate() {
        let (cts, stc) = default_handshake(&[&rustls::version::TLS12]);
        let tls = tag(&cts, &stc).unwrap();

        let ch = tls.client_hello.as_ref().unwrap();
        assert_eq!(ch.legacy_version, TlsVersion::Tls12);
        assert_eq!(ch.server_name.as_deref(), Some(TEST_SERVER_NAME));

        let sh = tls.server_hello.as_ref().unwrap();
        assert_eq!(sh.version, TlsVersion::Tls12);
        assert_eq!(sh.alpn.as_deref(), Some("h2"));
        assert!(!sh.session_resumed);

        assert_eq!(tls.server_certificates.len(), 2);

        assert_eq!(tls.server_certificates[0], TlsCertificate::from_der(TEST_LEAF_DER));
        assert_eq!(tls.server_certificates[0].fingerprint_sha256, TEST_LEAF_SHA256);
        assert_eq!(tls.server_certificates[0].subject, TEST_LEAF_SUBJECT);
        assert_eq!(tls.server_certificates[1], TlsCertificate::from_der(TEST_CA_DER));
        assert_eq!(tls.server_certificates[1].fingerprint_sha256, TEST_CA_SHA256);
        assert_eq!(tls.server_certificates[1].subject, TEST_CA_SUBJECT);

        assert!(tls.client_certificates.is_empty());
        assert!(!tls.client_certificate_requested);
        assert!(tls.alerts.is_empty());
        assert!(tls.client_change_cipher_spec);
        assert!(tls.server_change_cipher_spec);
        assert!(tls.client_application_data_records > 0);
        assert!(tls.server_application_data_records > 0);
    }

    #[test]
    fn parses_tls12_mutual_tls() {
        let (cts, stc) = run_handshake(
            client_config(ring::default_provider(), &[&rustls::version::TLS12], false, true),
            server_config(ring::default_provider(), &[&rustls::version::TLS12], true)
        );
        let tls = tag(&cts, &stc).unwrap();

        assert!(tls.client_certificate_requested);
        assert_eq!(tls.server_certificates.len(), 2);
        assert_eq!(tls.client_certificates.len(), 2);
        assert_eq!(tls.client_certificates[0].fingerprint_sha256, TEST_LEAF_SHA256);
        assert_eq!(tls.client_certificates[0].subject, TEST_LEAF_SUBJECT);
        assert_eq!(tls.client_certificates[1].fingerprint_sha256, TEST_CA_SHA256);
        assert!(tls.alerts.is_empty(), "{:?}", tls.alerts);
        assert!(tls.client_change_cipher_spec);
        assert!(tls.server_change_cipher_spec);
        assert!(tls.client_application_data_records > 0);
        assert!(tls.server_application_data_records > 0);
    }

    #[test]
    fn parses_rejected_certificate_alert() {
        let (cts, stc) = run_handshake(
            client_config(ring::default_provider(), &[&rustls::version::TLS12], true, false),
            server_config(ring::default_provider(), &[&rustls::version::TLS12], false)
        );
        let tls = tag(&cts, &stc).unwrap();

        assert!(tls.client_hello.is_some());
        assert!(tls.server_hello.is_some());
        assert_eq!(tls.server_certificates.len(), 2);

        assert_eq!(tls.alerts.len(), 1);
        let alert = &tls.alerts[0];
        assert_eq!(alert.direction, TlsDirection::ClientToServer);
        assert_eq!(alert.level, 2);
        assert_eq!(alert.description, 48);
        assert_eq!(alert.description_name, "unknown_ca");

        assert!(!tls.client_change_cipher_spec);
        assert_eq!(tls.client_application_data_records, 0);
    }

    fn hrr_handshake() -> (Vec<u8>, Vec<u8>) {
        // Client sends an X25519 key share first, server only accepts P-256.
        let client_provider = CryptoProvider {
            kx_groups: vec![ring::kx_group::X25519, ring::kx_group::SECP256R1],
            ..ring::default_provider()
        };
        let server_provider = CryptoProvider {
            kx_groups: vec![ring::kx_group::SECP256R1],
            ..ring::default_provider()
        };

        run_handshake(
            client_config(client_provider, &[&rustls::version::TLS13], false, false),
            server_config(server_provider, &[&rustls::version::TLS13], false)
        )
    }

    #[test]
    fn parses_hello_retry_request() {
        let (cts, stc) = hrr_handshake();

        // Two ClientHellos and two ServerHellos on the wire.
        assert_eq!(parse_direction(&cts, TlsDirection::ClientToServer).messages.iter()
            .filter(|(t, _)| *t == HANDSHAKE_CLIENT_HELLO).count(), 2);
        assert_eq!(parse_direction(&stc, TlsDirection::ServerToClient).messages.iter()
            .filter(|(t, _)| *t == HANDSHAKE_SERVER_HELLO).count(), 2);

        let tls = tag(&cts, &stc).unwrap();

        // Final hellos win.
        assert!(!tls.hello_retry_request);
        assert_eq!(tls.client_hello.unwrap().server_name.as_deref(), Some(TEST_SERVER_NAME));
        assert_eq!(tls.server_hello.unwrap().version, TlsVersion::Tls13);
        assert!(tls.alerts.is_empty());
        assert!(tls.client_application_data_records > 0);
        assert!(tls.server_application_data_records > 0);

        // Cut the stream after the HelloRetryRequest only.
        let hrr_end = RECORD_HEADER_LENGTH + u16::from_be_bytes([stc[3], stc[4]]) as usize;
        let tls = tag(&cts, &stc[..hrr_end]).unwrap();
        assert!(tls.hello_retry_request);
        assert!(tls.server_hello.is_some());
    }

    #[test]
    fn truncated_streams_never_panic_and_degrade_gracefully() {
        let (cts, stc) = default_handshake(&[&rustls::version::TLS12]);

        for cut in 0..=cts.len() {
            let result = tag(&cts[..cut], &stc);
            if cut > RECORD_HEADER_LENGTH {
                assert!(result.is_some(), "cut={}", cut);
            } else {
                assert!(result.is_none(), "cut={}", cut);
            }
        }

        for cut in 0..=stc.len() {
            match tag(&cts, &stc[..cut]) {
                Some(tls) => assert!(tls.client_hello.is_some(), "cut={}", cut),
                // A partial first record header from the server cannot be judged yet.
                None => assert!((1..RECORD_HEADER_LENGTH).contains(&cut), "cut={}", cut)
            }
        }

        // ClientHello header only: Tagged, but nothing parsed yet.
        let tls = tag(&cts[..RECORD_HEADER_LENGTH + 1], &[]).unwrap();
        assert!(tls.client_hello.is_none());
        assert!(tls.server_hello.is_none());

        // Full ClientHello but no response.
        let ch_end = RECORD_HEADER_LENGTH + u16::from_be_bytes([cts[3], cts[4]]) as usize;
        let tls = tag(&cts[..ch_end], &[]).unwrap();
        assert!(tls.client_hello.is_some());
        assert!(tls.server_hello.is_none());

        // First server record (ServerHello and certificates) cut in half: Nothing parsed yet.
        let first_record_end = RECORD_HEADER_LENGTH + u16::from_be_bytes([stc[3], stc[4]]) as usize;
        let tls = tag(&cts, &stc[..first_record_end / 2]).unwrap();
        assert!(tls.client_hello.is_some());
        assert!(tls.server_hello.is_none());
        assert!(tls.server_certificates.is_empty());
    }

    // Splits the body of the first record into records of `chunk` bytes each.
    fn refragment_first_record(buf: &[u8], chunk: usize) -> Vec<u8> {
        let length = u16::from_be_bytes([buf[3], buf[4]]) as usize;
        let body = &buf[RECORD_HEADER_LENGTH..RECORD_HEADER_LENGTH + length];

        let mut out = vec![];
        for part in body.chunks(chunk) {
            out.extend_from_slice(&buf[..3]);
            out.extend_from_slice(&(part.len() as u16).to_be_bytes());
            out.extend_from_slice(part);
        }
        out.extend_from_slice(&buf[RECORD_HEADER_LENGTH + length..]);
        out
    }

    #[test]
    fn reassembles_handshake_messages_fragmented_over_records() {
        let (cts, stc) = default_handshake(&[&rustls::version::TLS12]);
        let expected = tag(&cts, &stc).unwrap();
        assert_eq!(expected.server_certificates.len(), 2);

        for chunk in [1, 7, 64, 100, 517, 1000] {
            let fragmented_stc = refragment_first_record(&stc, chunk);
            assert_ne!(fragmented_stc, stc);
            let tls = tag(&cts, &fragmented_stc).unwrap();
            assert_eq!(tls, expected, "chunk={}", chunk);

            // And every truncation of the fragmented stream is still handled.
            for cut in 0..=fragmented_stc.len() {
                assert!(tag(&cts, &fragmented_stc[..cut]).is_some() || cut < RECORD_HEADER_LENGTH);
            }
        }
    }

    // Builds a TLS 1.0 ClientHello record without session ID or compression.
    fn build_client_hello(server_name: Option<&str>,
                          ciphers: &[u16],
                          extensions: &[(u16, Vec<u8>)]) -> Vec<u8> {
        let mut body = vec![0x03, 0x01]; // TLS 1.0
        body.extend_from_slice(&[0xAB; 32]); // Random.
        body.push(0); // No session ID.

        let mut cipher_bytes = vec![];
        for c in ciphers {
            cipher_bytes.extend_from_slice(&c.to_be_bytes());
        }
        body.extend_from_slice(&(cipher_bytes.len() as u16).to_be_bytes());
        body.extend_from_slice(&cipher_bytes);
        body.extend_from_slice(&[1, 0]); // One compression method: null.

        let mut ext_bytes = vec![];
        if let Some(name) = server_name {
            let mut sni = vec![];
            sni.extend_from_slice(&((name.len() + 3) as u16).to_be_bytes());
            sni.push(SNI_TYPE_HOST_NAME);
            sni.extend_from_slice(&(name.len() as u16).to_be_bytes());
            sni.extend_from_slice(name.as_bytes());
            ext_bytes.extend_from_slice(&EXT_SERVER_NAME.to_be_bytes());
            ext_bytes.extend_from_slice(&(sni.len() as u16).to_be_bytes());
            ext_bytes.extend_from_slice(&sni);
        }
        for (ext_type, data) in extensions {
            ext_bytes.extend_from_slice(&ext_type.to_be_bytes());
            ext_bytes.extend_from_slice(&(data.len() as u16).to_be_bytes());
            ext_bytes.extend_from_slice(data);
        }
        body.extend_from_slice(&(ext_bytes.len() as u16).to_be_bytes());
        body.extend_from_slice(&ext_bytes);

        let mut message = vec![HANDSHAKE_CLIENT_HELLO];
        message.extend_from_slice(&(body.len() as u32).to_be_bytes()[1..]);
        message.extend_from_slice(&body);

        let mut record = vec![RECORD_TYPE_HANDSHAKE, 0x03, 0x01];
        record.extend_from_slice(&(message.len() as u16).to_be_bytes());
        record.extend_from_slice(&message);
        record
    }

    fn u16_list_bytes(values: &[u16]) -> Vec<u8> {
        let mut bytes = vec![];
        for v in values {
            bytes.extend_from_slice(&v.to_be_bytes());
        }
        let mut out = (bytes.len() as u16).to_be_bytes().to_vec();
        out.extend_from_slice(&bytes);
        out
    }

    #[test]
    fn parses_legacy_client_hello() {
        let ciphers = [47, 53, 5, 10, 49161, 49162, 49171, 49172, 50, 56, 19, 4];
        let extensions = vec![
            (EXT_SUPPORTED_GROUPS, u16_list_bytes(&[23, 24, 25])),
            (EXT_EC_POINT_FORMATS, vec![1, 0])
        ];

        let cts = build_client_hello(Some("example.com"), &ciphers, &extensions);
        let tls = tag(&cts, &[]).unwrap();
        let ch = tls.client_hello.unwrap();

        assert_eq!(ch.record_version, TlsVersion::Tls10);
        assert_eq!(ch.legacy_version, TlsVersion::Tls10);
        assert_eq!(ch.server_name.as_deref(), Some("example.com"));
        assert_eq!(ch.cipher_suites, ciphers);
        assert_eq!(ch.extensions, vec![EXT_SERVER_NAME, EXT_SUPPORTED_GROUPS, EXT_EC_POINT_FORMATS]);
        assert_eq!(ch.supported_groups, vec![23, 24, 25]);
        assert_eq!(ch.ec_point_formats, vec![0]);
        assert!(ch.supported_versions.is_empty());
        assert!(ch.signature_algorithms.is_empty());
        assert!(ch.alpn.is_empty());
        assert!(!ch.session_id_present);
    }

    #[test]
    fn grease_values_are_ignored() {
        let ciphers = [0x0A0A, 0x1301, 0x1302];
        let extensions = vec![
            (0x1A1A, vec![]),
            (EXT_SUPPORTED_GROUPS, u16_list_bytes(&[0x2A2A, 0x001D])),
            (EXT_SUPPORTED_VERSIONS, {
                let mut v = vec![6];
                v.extend_from_slice(&[0x3A, 0x3A, 0x03, 0x04, 0x03, 0x03]);
                v
            })
        ];

        let cts = build_client_hello(None, &ciphers, &extensions);
        let ch = tag(&cts, &[]).unwrap().client_hello.unwrap();

        assert_eq!(ch.cipher_suites, vec![0x1301, 0x1302]);
        assert_eq!(ch.extensions, vec![EXT_SUPPORTED_GROUPS, EXT_SUPPORTED_VERSIONS]);
        assert_eq!(ch.supported_groups, vec![0x001D]);
        assert_eq!(ch.supported_versions, vec![TlsVersion::Tls13, TlsVersion::Tls12]);
        assert!(ch.server_name.is_none());
    }

    #[test]
    fn rejects_malformed_complete_client_hello() {
        // Valid record and handshake headers, but the body is garbage.
        let mut cts = vec![RECORD_TYPE_HANDSHAKE, 0x03, 0x01, 0x00, 0x14, HANDSHAKE_CLIENT_HELLO, 0x00, 0x00, 0x10];
        cts.extend_from_slice(&[0xFF; 16]);
        assert!(tag(&cts, &[]).is_none());
    }

    #[test]
    fn parses_alerts_in_both_directions() {
        let tls = tag(CLIENT_HELLO, ALERT).unwrap();
        assert_eq!(tls.alerts.len(), 1);
        assert_eq!(tls.alerts[0].direction, TlsDirection::ServerToClient);
        assert_eq!(tls.alerts[0].level, 2);
        assert_eq!(tls.alerts[0].description_name, "handshake_failure");
    }

    #[test]
    fn tags_full_handshake() {
        assert!(tag(CLIENT_HELLO, SERVER_HELLO).is_some());
    }

    #[test]
    fn tags_client_hello_without_response() {
        assert!(tag(CLIENT_HELLO, &[]).is_some());
    }

    #[test]
    fn tags_client_hello_answered_with_alert() {
        assert!(tag(CLIENT_HELLO, ALERT).is_some());
    }

    #[test]
    fn rejects_server_hello_without_client_hello() {
        assert!(tag(&[], SERVER_HELLO).is_none());
    }

    #[test]
    fn rejects_non_tls_response() {
        assert!(tag(CLIENT_HELLO, b"HTTP/1.1 400 Bad Request\r\n").is_none());
    }

    #[test]
    fn rejects_plaintext() {
        assert!(tag(b"GET / HTTP/1.1\r\n", b"HTTP/1.1 200 OK\r\n").is_none());
        assert!(tag(b"SSH-2.0-OpenSSH_9.6\r\n", b"SSH-2.0-OpenSSH_9.6\r\n").is_none());
    }

    #[test]
    fn rejects_application_data_record_as_first_record() {
        assert!(tag(&[0x17, 0x03, 0x03, 0x00, 0x10, 0x01], &[]).is_none());
    }

    #[test]
    fn rejects_bad_version_or_length() {
        assert!(tag(&[0x16, 0x02, 0x01, 0x00, 0x05, 0x01], &[]).is_none());
        assert!(tag(&[0x16, 0x03, 0x01, 0x00, 0x00, 0x01], &[]).is_none());
        assert!(tag(&[0x16, 0x03, 0x01, 0xFF, 0xFF, 0x01], &[]).is_none());
    }

    #[test]
    fn rejects_short_buffers() {
        assert!(tag(&[0x16, 0x03], &[]).is_none());
        assert!(tag(&[], &[]).is_none());
    }
}

