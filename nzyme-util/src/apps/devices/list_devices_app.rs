use crate::exit_codes::EX_PERMISSION_DENIED;
use crate::usb::usb::detect_nzyme_usb_devices;

pub fn run() {

    let devices = match detect_nzyme_usb_devices() {
        Ok(devices) => devices,
        Err(e) => {
            eprintln!("Could not detect Nzyme USB devices. Make sure you have the \
                required permissions. Error: {}", e);
            std::process::exit(EX_PERMISSION_DENIED);
        }
    };

    for device in devices {
        println!("{}\n", device);
    }


}