use anyhow::Error;
use crate::peripherals::cobs::cobs_decode;

pub struct SonaFramer {
    acc: Vec<u8>,
    max_size: usize,
}

#[derive(Debug)]
pub enum SonaFramerError {
    // Too many bytes accumulated without a delimiter. Buffer was cleared.
    Overflow,
}

impl SonaFramer {
    pub fn new(max_size: usize) -> Self {
        Self { acc: Vec::with_capacity(max_size), max_size }
    }

    /*
     * Feed raw bytes from the wire. On overflow the buffer is cleared and
     * an error is returned so the caller can log/meter it.
     */
    pub fn push(&mut self, bytes: &[u8]) -> Result<(), SonaFramerError> {
        if self.acc.len() + bytes.len() > self.max_size {
            self.acc.clear();
            return Err(SonaFramerError::Overflow);
        }
        self.acc.extend_from_slice(bytes);
        Ok(())
    }

    /*
     * Pop the next complete frame.
     *
     * - `None`: no delimiter seen yet, need more bytes.
     * - `Some(Ok(decoded))`: a frame decoded successfully.
     * - `Some(Err(e))`: a complete frame was found but COBS-decode failed
     *   (expected once at startup after sync, otherwise rare corruption)
     */
    pub fn next_frame(&mut self) -> Option<Result<Vec<u8>, Error>> {
        loop {
            let pos = self.acc.iter().position(|&b| b == 0)?;
            if pos == 0 {
                // Empty segment between two delimiters. Skip and keep looking.
                self.acc.drain(0..=pos);
                continue;
            }
            let result = cobs_decode(&self.acc[..pos]);
            self.acc.drain(0..=pos);
            return Some(result);
        }
    }

    /*
     * Pull every complete frame currently in the buffer, handing each to
     * `handler`. Stops when no more delimiters are present.
     */
    pub fn drain<F>(&mut self, mut handler: F)
    where
        F: FnMut(Result<Vec<u8>, Error>),
    {
        while let Some(frame) = self.next_frame() {
            handler(frame);
        }
    }
}