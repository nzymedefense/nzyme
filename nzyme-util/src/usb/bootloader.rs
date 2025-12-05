use anyhow::{Context, Result};
use std::io::Write;
use std::time::Duration;

const NZ_MAGIC0: u8 = b'N';
const NZ_MAGIC1: u8 = b'Z';
const NZ_VER_CONTROL: u8 = 0x01 ;
const CMD_ENTER_BOOTLOADER: u8 = 0x01;

fn crc16(data: &[u8]) -> u16 {
    let mut crc: u16 = 0xFFFF;

    for &byte in data {
        crc ^= (byte as u16) << 8;
        for _ in 0..8 {
            if (crc & 0x8000) != 0 {
                crc = (crc << 1) ^ 0x1021;
            } else {
                crc <<= 1;
            }
            crc &= 0xFFFF;
        }
    }

    crc
}


pub fn send_enter_bootloader<P: AsRef<std::path::Path>>(dev_path: P) -> Result<()> {
    let dev_str = dev_path
        .as_ref()
        .to_str()
        .context("device path is not valid UTF-8")?
        .to_string();

    // Open the CDC ACM device. Baud rate is irrelevant for USB CDC, but
    // some stacks insist on it being set; 115200 is a safe default.
    let mut port = serialport::new(&dev_str, 115_200)
        .timeout(Duration::from_secs(1))
        .open()
        .with_context(|| format!("failed to open {}", dev_str))?;

    // Build [magic0, magic1, version, cmd]
    let header = [
        NZ_MAGIC0,
        NZ_MAGIC1,
        NZ_VER_CONTROL,
        CMD_ENTER_BOOTLOADER,
    ];

    // CRC is computed over [version, cmd] per your C code:
    //   uint8_t crc_input[2] = { frame.version, frame.cmd };
    let crc = crc16(&header[2..4]);

    // Frame layout must match nz_control_frame_t in memory:
    // struct {
    //   uint8_t magic0;
    //   uint8_t magic1;
    //   uint8_t version;
    //   uint8_t cmd;
    //   uint16_t crc;  // little-endian on Cortex-M
    // };
    let mut frame: [u8; 6] = [0; 6];
    frame[0] = header[0];
    frame[1] = header[1];
    frame[2] = header[2];
    frame[3] = header[3];
    frame[4] = (crc & 0x00FF) as u8;        // low byte
    frame[5] = ((crc >> 8) & 0x00FF) as u8; // high byte

    // Write the frame
    port.write_all(&frame)
        .context("failed to write enter-bootloader frame")?;

    // Make a best-effort flush
    port.flush().ok();

    // Port is closed when `port` is dropped.
    Ok(())
}