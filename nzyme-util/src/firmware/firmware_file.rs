use std::fs;

use crate::exit_codes::{EX_IOERR, EX_UNAVAILABLE};
use crate::firmware::firmware::{
    V1_HEADER_LEN,
};
use crate::tools::as_path_buf;

#[allow(dead_code)]
pub struct FirmwareFile {
    pub path: String,
    pub total_size: usize,

    pub magic: [u8; 2],
    pub header_version: u8,
    pub file_type: u8,
    pub header_len: u16,
    pub firmware_len: u32,
    pub usb_vid: u16,
    pub usb_pid: u16,
    pub signing_key_id: u16,
    pub fw_major: u8,
    pub fw_minor: u8,

    pub header_without_signature: [u8; 20],
    pub signature: [u8; 64],
    pub firmware_payload: Vec<u8>,
}

pub fn load_firmware_file(path: String) -> FirmwareFile {
    const RESET: &str = "\x1b[0m";
    const FG_RED: &str = "\x1b[31m";

    let data = match fs::read(&as_path_buf(&path)) {
        Ok(data) => data,
        Err(e) => {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Could not load firmware file [{path}].");
            eprintln!("    Details: {}", e);
            std::process::exit(EX_IOERR);
        }
    };

    if data.len() < V1_HEADER_LEN as usize {
        eprintln!("{FG_RED}[x] ERROR:{RESET} File [{path}] too small to contain firmware header.");
        eprintln!(
            "    File size: {} bytes, required at least: {} bytes",
            data.len(),
            V1_HEADER_LEN
        );
        std::process::exit(EX_UNAVAILABLE);
    }

    let header = &data[..V1_HEADER_LEN as usize];

    let read_u16 = |offset: usize| -> u16 {
        u16::from_le_bytes([header[offset], header[offset + 1]])
    };
    let read_u32 = |offset: usize| -> u32 {
        u32::from_le_bytes([
            header[offset],
            header[offset + 1],
            header[offset + 2],
            header[offset + 3],
        ])
    };

    // Parse header fields.
    let magic = [header[0], header[1]];
    let header_version = header[2];
    let file_type = header[3];
    let header_len = read_u16(4);
    let firmware_len = read_u32(6);
    let usb_vid = read_u16(10);
    let usb_pid = read_u16(12);
    let signing_key_id = read_u16(14);
    let fw_major = header[16];
    let fw_minor = header[17];

    // Structural check: header_len must match our version.
    if header_len != V1_HEADER_LEN {
        eprintln!("{FG_RED}[x] ERROR:{RESET} Header length mismatch in [{path}].");
        eprintln!("    Got:   {}", header_len);
        eprintln!("    Expect {}", V1_HEADER_LEN);
        std::process::exit(EX_UNAVAILABLE);
    }

    // Structural check: total file size must be at least header + firmware_len.
    let total_expected = V1_HEADER_LEN as usize + firmware_len as usize;
    if data.len() < total_expected {
        eprintln!("{FG_RED}[x] ERROR:{RESET} File [{path}] truncated.");
        eprintln!(
            "    File size: {} bytes, but header claims: {} bytes",
            data.len(),
            total_expected
        );
        std::process::exit(EX_UNAVAILABLE);
    }

    // Split out header_without_sig and signature.
    let mut header_without_signature = [0u8; 20];
    header_without_signature.copy_from_slice(&header[0..20]);

    let mut signature = [0u8; 64];
    signature.copy_from_slice(&header[20..84]);

    // Extract firmware payload.
    let firmware_payload = data[V1_HEADER_LEN as usize .. total_expected].to_vec();

    FirmwareFile {
        path,
        total_size: data.len(),
        magic,
        header_version,
        file_type,
        header_len,
        firmware_len,
        usb_vid,
        usb_pid,
        signing_key_id,
        fw_major,
        fw_minor,
        header_without_signature,
        signature,
        firmware_payload,
    }
}
