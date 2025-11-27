mod exit_codes;
mod connect;
mod firmware;
mod usb;
mod ui;

use crate::connect::connect_firmware_directory::fetch_firmware_directory;
use crate::exit_codes::{EX_INTERNAL_ERR, EX_OK, EX_PERMISSION_DENIED, EX_UNAVAILABLE};
use crate::ui::tui::tui_app::TuiApp;
use crate::usb::usb::detect_nzyme_usb_devices;

fn main() {
    let directory = match fetch_firmware_directory() {
        Ok(directory) => directory,
        Err(e) => {
            eprintln!("ERROR: Could not fetch Nzyme device directory \
                from [https://connect.nzyme.org/]: {}" ,e);
            std::process::exit(EX_UNAVAILABLE);
        }
    };

    let devices = match detect_nzyme_usb_devices() {
        Ok(devices) => devices,
        Err(e) => {
            eprintln!("ERROR: Could not discover connected Nzyme USB devices: {}" ,e);
            std::process::exit(EX_PERMISSION_DENIED);
        }
    };

    if let Err(e) = color_eyre::install() {
        eprintln!("ERROR: {}", e);
    }

    let terminal = ratatui::init();

    if let Err(e) = TuiApp::new(devices).run(terminal) {
        eprintln!("ERROR: {}", e);
        std::process::exit(EX_INTERNAL_ERR);

    }

    ratatui::restore();
    std::process::exit(EX_OK);
}