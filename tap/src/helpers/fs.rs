use std::fs::create_dir_all;
use std::path::PathBuf;
use anyhow::{bail, Error};

static TAP_SUBDIRECTORY: &str = "tap";

fn get_tap_data_subdirectory_path(data_directory: &PathBuf) -> PathBuf {
    data_directory.join(TAP_SUBDIRECTORY)
}

pub fn ensure_tap_data_subdirectory(data_directory: &PathBuf) -> Result<(), Error> {
    let full_path = get_tap_data_subdirectory_path(data_directory);

    match create_dir_all(full_path) {
        Ok(_) => Ok(()),
        Err(e) => bail!("Could not create \"tap/\" directory in data directory: [{}]", e)
    }
}

pub fn get_tap_data_type_path(data_directory: &PathBuf, data_type: &str) -> PathBuf {
    get_tap_data_subdirectory_path(data_directory).join(data_type)
}

pub fn ensure_tap_data_type_subdirectory(data_directory: &PathBuf, data_type: &str)
    -> Result<(), Error> {
    let full_data_path = get_tap_data_type_path(data_directory, data_type);

    match create_dir_all(full_data_path) {
        Ok(_) => Ok(()),
        Err(e) => bail!("Could not create \"{}\" data type directory in tap data directory: [{}]",
            data_type, e)
    }
}