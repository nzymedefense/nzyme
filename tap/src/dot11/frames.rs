#[derive(Debug)]

pub struct Dot11Frame {
    pub data: Vec<u8>
}

pub struct Dot11ManagementFrame {
    pub header: Vec<u8>,
    pub payload: Vec<u8>,
}