use std::fs;
use anyhow::{bail, Error};

pub fn load_firmware_from_relative_path(firmware_file_path: &str) -> Result<Vec<u8>, Error>{
    // Resolve firmware path relative to the running executable.
    let exe_path = match std::env::current_exe() {
        Ok(path) => path,
        Err(e) => {
            bail!("Could not get own path: {}", e);
        }
    };

    let exe_dir = match exe_path.parent() {
        Some(dir) => dir,
        None => {
            bail!("Own path has no parent.");
        }
    };

    let firmware_path = exe_dir.join(firmware_file_path);

    // Read firmware into memory.
    let firmware = match fs::read(&firmware_path) {
        Ok(bytes) => bytes,
        Err(e) => {
            bail!("Could not read firmware file [{}]: {}", firmware_path.display(), e);
        }
    };

    if firmware.is_empty() {
        bail!("Firmware file [{}] is empty.", firmware_path.display());
    }

    if firmware.len() > u32::MAX as usize {
        bail!("Firmware file [{}] is too large.", firmware_path.display());
    }

    Ok(firmware)
}