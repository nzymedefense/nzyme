const RECORD_TYPE_HANDSHAKE: u8 = 0x16;
const RECORD_TYPE_ALERT: u8 = 0x15;
const RECORD_VERSION_MAJOR: u8 = 0x03; // SSL 3.0 and all TLS versions.
const RECORD_VERSION_MINOR_MAX: u8 = 0x04; // TLS 1.3.
const HANDSHAKE_CLIENT_HELLO: u8 = 0x01;

const MAX_RECORD_LENGTH: usize = 16384;

const RECORD_HEADER_LENGTH: usize = 5;

pub fn tag(cts: &[u8], stc: &[u8]) -> Option<()> {
    // Client must have started with a ClientHello.
    if !is_record_header(cts, &[RECORD_TYPE_HANDSHAKE])
        || cts.get(RECORD_HEADER_LENGTH) != Some(&HANDSHAKE_CLIENT_HELLO) {
        return None;
    }
    
    if !stc.is_empty() && !is_record_header(stc, &[RECORD_TYPE_HANDSHAKE, RECORD_TYPE_ALERT]) {
        return None;
    }

    Some(())
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

#[cfg(test)]
mod tests {
    use super::*;

    // TLS 1.2 record carrying a (truncated) ClientHello.
    const CLIENT_HELLO: &[u8] = &[0x16, 0x03, 0x01, 0x00, 0xA5, 0x01, 0x00, 0x00, 0xA1, 0x03, 0x03];
    const SERVER_HELLO: &[u8] = &[0x16, 0x03, 0x03, 0x00, 0x5A, 0x02, 0x00, 0x00, 0x56, 0x03, 0x03];
    const ALERT: &[u8] = &[0x15, 0x03, 0x03, 0x00, 0x02, 0x02, 0x28];

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
