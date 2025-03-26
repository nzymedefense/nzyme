#[derive(Debug)]
pub struct BufferCursor<'a> {
    buf: &'a [u8],
    offset: usize,
}

// Copy free implementation of a buffer that always check if the cursor is going out of bounds before returning data
impl<'a> BufferCursor<'a> {
    pub fn new(buf: &'a [u8]) -> Self {
        Self { buf, offset: 0 }
    }

    pub fn remaining(&self) -> usize {
        self.buf.len().saturating_sub(self.offset)
    }

    pub fn align(&mut self, align: usize) {
        let misalignment = self.offset % align;
        if misalignment != 0 {
            self.offset += align - misalignment;
        }
    }

    pub fn take(&mut self, len: usize) -> Option<&'a [u8]> {
        if self.offset + len > self.buf.len() {
            None
        } else {
            let slice = &self.buf[self.offset..self.offset + len];
            self.offset += len;
            Some(slice)
        }
    }

    pub fn take_array<const N: usize>(&mut self) -> Option<&'a [u8; N]> {
        self.take(N)?.try_into().ok()
    }

    pub fn position(&self) -> usize {
        self.offset
    }

    pub fn slice_from_current(&self) -> &'a [u8] {
        &self.buf[self.offset..]
    }
}