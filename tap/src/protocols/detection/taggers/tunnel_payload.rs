#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct TunnelPayload {
    pub client_to_server_offset: usize,
    pub server_to_client_offset: usize
}

impl TunnelPayload {

    pub fn new(client_to_server_offset: usize, server_to_client_offset: usize) -> Self {
        Self { client_to_server_offset, server_to_client_offset }
    }

    pub fn none(cts: &[u8], stc: &[u8]) -> Self {
        Self::new(cts.len(), stc.len())
    }

    pub fn slice<'a>(&self, cts: &'a [u8], stc: &'a [u8]) -> (&'a [u8], &'a [u8]) {
        (
            &cts[self.client_to_server_offset.min(cts.len())..],
            &stc[self.server_to_client_offset.min(stc.len())..]
        )
    }

}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn slices_payload_after_offsets() {
        let cts = b"HANDSHAKEpayload";
        let stc = b"OKdata";
        let (c, s) = TunnelPayload::new(9, 2).slice(cts, stc);
        assert_eq!(c, b"payload");
        assert_eq!(s, b"data");
    }

    #[test]
    fn offsets_beyond_buffer_yield_empty_slices() {
        let (c, s) = TunnelPayload::new(100, 100).slice(b"abc", b"de");
        assert!(c.is_empty());
        assert!(s.is_empty());
    }

    #[test]
    fn none_yields_empty_slices() {
        let (c, s) = TunnelPayload::none(b"abc", b"de").slice(b"abc", b"de");
        assert!(c.is_empty());
        assert!(s.is_empty());
    }
}
