use std::sync::{Arc, Mutex};

use rustls::client::danger::{HandshakeSignatureValid, ServerCertVerified, ServerCertVerifier};
use rustls::pki_types::{CertificateDer, ServerName, UnixTime};
use rustls::{ClientConfig, DigitallySignedStruct, SignatureScheme};
use crate::wired::tls::certificate::TlsCertificate;
use crate::wired::tls::version::TlsVersion;

#[derive(Debug, Clone, Default)]
pub struct CapturedTls {
    // Full chain, leaf first, as raw DER and parsed.
    pub chain_der: Vec<Vec<u8>>,
    pub chain: Vec<TlsCertificate>,
    pub leaf_sha256: String,
    pub protocol_version: Option<TlsVersion>,
    pub cipher_suite: Option<u16>,
    pub sni: Option<String>,
}

pub type TlsSlot = Arc<Mutex<Option<CapturedTls>>>;

#[derive(Debug)]
struct CaptureVerifier {
    slot: TlsSlot,
}

impl CaptureVerifier {
    fn capture(&self, end_entity: &CertificateDer<'_>, intermediates: &[CertificateDer<'_>]) {
        let mut rec = CapturedTls::default();

        for der in std::iter::once(end_entity).chain(intermediates.iter()) {
            rec.chain_der.push(der.as_ref().to_vec());
            rec.chain.push(TlsCertificate::from_der(der.as_ref()));
        }

        rec.leaf_sha256 = rec.chain[0].fingerprint_sha256.clone();

        if let Ok(mut guard) = self.slot.lock() {
            *guard = Some(rec);
        }
    }
}

impl ServerCertVerifier for CaptureVerifier {
    fn verify_server_cert(
        &self,
        end_entity: &CertificateDer<'_>,
        intermediates: &[CertificateDer<'_>],
        _server_name: &ServerName<'_>,
        _ocsp_response: &[u8],
        _now: UnixTime,
    ) -> Result<ServerCertVerified, rustls::Error> {
        self.capture(end_entity, intermediates);
        Ok(ServerCertVerified::assertion())
    }

    fn verify_tls12_signature(
        &self,
        _message: &[u8],
        _cert: &CertificateDer<'_>,
        _dss: &DigitallySignedStruct,
    ) -> Result<HandshakeSignatureValid, rustls::Error> {
        Ok(HandshakeSignatureValid::assertion())
    }

    fn verify_tls13_signature(
        &self,
        _message: &[u8],
        _cert: &CertificateDer<'_>,
        _dss: &DigitallySignedStruct,
    ) -> Result<HandshakeSignatureValid, rustls::Error> {
        Ok(HandshakeSignatureValid::assertion())
    }

    fn supported_verify_schemes(&self) -> Vec<SignatureScheme> {
        vec![
            SignatureScheme::RSA_PKCS1_SHA256,
            SignatureScheme::RSA_PKCS1_SHA384,
            SignatureScheme::RSA_PKCS1_SHA512,
            SignatureScheme::ECDSA_NISTP256_SHA256,
            SignatureScheme::ECDSA_NISTP384_SHA384,
            SignatureScheme::RSA_PSS_SHA256,
            SignatureScheme::RSA_PSS_SHA384,
            SignatureScheme::RSA_PSS_SHA512,
            SignatureScheme::ED25519,
        ]
    }
}

pub fn install_crypto_provider() {
    let _ = rustls::crypto::ring::default_provider().install_default();
}

pub fn client_config(slot: TlsSlot) -> Arc<ClientConfig> {
    let verifier = Arc::new(CaptureVerifier { slot });
    let config = ClientConfig::builder()
        .dangerous()
        .with_custom_certificate_verifier(verifier)
        .with_no_client_auth();
    Arc::new(config)
}