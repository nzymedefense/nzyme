use anyhow::{bail, Context, Error, Result};
use std::io::Write;
use std::thread::sleep;
use std::time::{Duration, Instant};

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

    let mut port = serialport::new(&dev_str, 115_200)
        .timeout(Duration::from_secs(1))
        .open()
        .with_context(|| format!("failed to open {}", dev_str))?;

    let header = [
        NZ_MAGIC0,
        NZ_MAGIC1,
        NZ_VER_CONTROL,
        CMD_ENTER_BOOTLOADER,
    ];

    let crc = crc16(&header[2..4]);

    let mut frame: [u8; 6] = [0; 6];
    frame[0] = header[0];
    frame[1] = header[1];
    frame[2] = header[2];
    frame[3] = header[3];
    frame[4] = (crc & 0x00FF) as u8;
    frame[5] = ((crc >> 8) & 0x00FF) as u8;

    // Write the frame.
    port.write_all(&frame)
        .context("failed to write enter-bootloader frame")?;

    // Make a best-effort flush.
    port.flush().ok();

    // Port is closed when `port` is dropped.

    Ok(())
}

pub fn flash_firmware(acm_port: &str, firmware: Vec<u8>) -> Result<(), Error> {
    // Bootloader command header.
    const NZ_MAGIC0: u8 = b'N';
    const NZ_MAGIC1: u8 = b'Z';
    const NZ_VER_BOOTLOADER: u8 = 0x01;
    const CMD_FLASH_APP: u8 = 0x10;

    let app_size = firmware.len() as u32;

    let mut header = [0u8; 8];
    header[0] = NZ_MAGIC0;
    header[1] = NZ_MAGIC1;
    header[2] = NZ_VER_BOOTLOADER;
    header[3] = CMD_FLASH_APP;
    header[4..8].copy_from_slice(&app_size.to_le_bytes());

    /*
     * We should be in bootloader mode already, with ACM port available. Just in case, we wait
     * a while for it to become available, but this should really return immediately if this
     * function is used correctly.
     */
    let start = Instant::now();
    let mut port = loop {
        match serialport::new(acm_port, 115_200)
            .timeout(Duration::from_millis(2000))
            .open()
        {
            Ok(p) => break p,
            Err(e) => {
                if start.elapsed() > Duration::from_secs(9) {
                    bail!("Unable to open [{}] for bootloader within timeout: {e}", acm_port)
                }

                // Wait a bit and retry.
                sleep(Duration::from_millis(200));
            }
        }
    };

    // Send header.
    port.write_all(&header)?;
    port.flush()?;

    // Stream firmware image.
    const CHUNK_SIZE: usize = 4096;
    let mut offset = 0;

    while offset < firmware.len() {
        let end = (offset + CHUNK_SIZE).min(firmware.len());
        port.write_all(&firmware[offset..end])?;
        offset = end;

        // Give the MCU some time to drain its RX buffer, just in case.
        std::thread::sleep(Duration::from_millis(2));
    }
    port.flush()?;

    Ok(())
}