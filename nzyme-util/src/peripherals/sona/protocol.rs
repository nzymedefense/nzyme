use crate::peripherals::cobs::cobs_encode;
use crate::peripherals::crc::crc16_ccitt;
use crate::peripherals::sona::frame_header::SonaFrameHeader;
use crate::peripherals::sona::metrics::{parse_metrics_payload, SonaMetrics};

const WIRE_PROTOCOL_VERSION: u8 = 2;

pub const MSG_TYPE_DOT11: u8 = 1;
const MSG_TYPE_CMD: u8 = 2;
const MSG_TYPE_METRICS: u8 = 3;

const CMD_SET_FREQUENCY: u8 = 1;
#[allow(dead_code)]
const CMD_ENTER_BOOTLOADER: u8 = 2;
#[allow(dead_code)]
const CMD_SET_FILTER: u8 = 3;

const HEADER_BYTES: usize = 6;

#[derive(Debug)]
pub enum SonaMessage {
    Dot11 { header: SonaFrameHeader, frame_len: usize },
    Metrics(SonaMetrics),
    Unknown(u8),
}

#[derive(Debug)]
pub enum DecodeError {
    TooShort,
    BadVersion(u8),
    LengthMismatch { declared: usize, got: usize },
    CrcMismatch { declared: u16, computed: u16 },
    BadMetrics,
}

pub fn parse_message(decoded: &[u8]) -> Result<SonaMessage, DecodeError> {
    if decoded.len() < HEADER_BYTES {
        return Err(DecodeError::TooShort);
    }

    let msg_type = decoded[0];
    let version = decoded[1];
    let payload_len = u16::from_le_bytes([decoded[2], decoded[3]]) as usize;
    let crc_in_hdr = u16::from_le_bytes([decoded[4], decoded[5]]);

    if version != WIRE_PROTOCOL_VERSION {
        return Err(DecodeError::BadVersion(version));
    }

    if decoded.len() != HEADER_BYTES + payload_len {
        return Err(DecodeError::LengthMismatch {
            declared: HEADER_BYTES + payload_len,
            got: decoded.len(),
        });
    }

    let payload = &decoded[HEADER_BYTES..HEADER_BYTES + payload_len];

    let computed = crc16_ccitt(payload);
    if computed != crc_in_hdr {
        return Err(DecodeError::CrcMismatch { declared: crc_in_hdr, computed });
    }

    match msg_type {
        MSG_TYPE_DOT11 => {
            if payload.len() < SonaFrameHeader::BYTES {
                return Err(DecodeError::TooShort);
            }
            let header = SonaFrameHeader::parse(&payload[..SonaFrameHeader::BYTES])
                .ok_or(DecodeError::TooShort)?;
            let frame_len = payload.len() - SonaFrameHeader::BYTES;
            Ok(SonaMessage::Dot11 { header, frame_len })
        }
        MSG_TYPE_METRICS => parse_metrics_payload(payload)
            .map(SonaMessage::Metrics)
            .ok_or(DecodeError::BadMetrics),
        other => Ok(SonaMessage::Unknown(other)),
    }
}

fn send_command(port: &mut dyn serialport::SerialPort, payload: &[u8]) -> std::io::Result<()> {
    let crc = crc16_ccitt(payload);

    let mut msg = Vec::with_capacity(HEADER_BYTES + payload.len());
    msg.push(MSG_TYPE_CMD);
    msg.push(WIRE_PROTOCOL_VERSION);
    msg.extend_from_slice(&(payload.len() as u16).to_le_bytes());
    msg.extend_from_slice(&crc.to_le_bytes());
    msg.extend_from_slice(payload);

    let mut enc = cobs_encode(&msg);
    enc.push(0u8);

    port.write_all(&enc)
}

pub fn send_set_frequency(port: &mut dyn serialport::SerialPort, freq_mhz: u16) -> std::io::Result<()> {
    let payload = [
        CMD_SET_FREQUENCY,
        (freq_mhz & 0xFF) as u8,
        (freq_mhz >> 8) as u8,
    ];
    send_command(port, &payload)
}