use std::thread::sleep;
use std::time::{Duration, Instant};
use crate::exit_codes::{EX_PERMISSION_DENIED, EX_UNAVAILABLE};
use crate::firmware::firmware_loader::load_firmware_from_relative_path;
use crate::usb::bootloader::{flash_firmware, send_enter_bootloader};
use crate::usb::usb::{detect_nzyme_usb_devices, nzyme_device_is_connected, NZYME_BOOTLOADER_PID, NZYME_VID};

pub fn run(firmware_file: String, device_serial: String) {
    const RESET: &str = "\x1b[0m";
    const BOLD: &str = "\x1b[1m";
    const FG_RED: &str = "\x1b[31m";
    const FG_GREEN: &str = "\x1b[32m";
    const FG_YELLOW: &str = "\x1b[33m";
    const FG_CYAN: &str = "\x1b[36m";

    println!("{BOLD}==> Nzyme Firmware Upgrade{RESET}");
    println!("    Device Serial : {device_serial}");
    println!("    Firmware File : {firmware_file}\n");

    // Detect devices
    let devices = match detect_nzyme_usb_devices() {
        Ok(devices) => devices,
        Err(e) => {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Could not detect Nzyme USB devices.");
            eprintln!("    Details: {}", e);
            std::process::exit(EX_PERMISSION_DENIED);
        }
    };

    // Find device
    let device = match devices.into_iter().find(|d| d.serial == device_serial) {
        Some(d) => d,
        None => {
            eprintln!("{FG_RED}[x] ERROR:{RESET} No device with serial [{device_serial}] found.");
            std::process::exit(EX_UNAVAILABLE);
        }
    };

    println!("{FG_CYAN}Device Information:{RESET}");
    println!("    Product        : {}", device.product);
    println!("    Manufacturer   : {}", device.manufacturer);
    println!("    Serial Number  : {}", device.serial);
    println!("    Firmware       : v{}.{}",
             device.firmware_version.major, device.firmware_version.minor);
    println!("    USB VID:PID    : {:04X}:{:04X}", device.vid, device.pid);

    let acm_port = match device.acm_port {
        Some(port) => port,
        None => {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Device exposes no ACM port.");
            std::process::exit(EX_UNAVAILABLE);
        }
    };

    println!("    ACM Port       : {acm_port}\n");

    println!("{BOLD}==> Loading Firmware{RESET}");
    let firmware = match load_firmware_from_relative_path(&firmware_file) {
        Ok(f) => {
            println!("{FG_GREEN}[*] Loaded firmware image. Size <{}> byte.{RESET}", f.len());
            f
        }
        Err(e) => {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Cannot load firmware file.");
            eprintln!("    Details: {}", e);
            std::process::exit(EX_UNAVAILABLE);
        }
    };

    if device.vid == NZYME_VID && device.pid == NZYME_BOOTLOADER_PID {
        /*
         * We are talking to a Nzyme bootloader. It's likely in the failure loop after it couldn't
         * initialize the app or app has not successfully initialized on previous boot.
         *
         * In that case, we do not attempt to put app into bootloader mode (we are already in
         * bootloader), but start to flash firmware immediately.
         */
        println!("{FG_YELLOW}[!!] We are connected to bootloader. Do not issue \
            bootloader mode reset command.{RESET}");
    } else {
        // Put device into bootloader mode.
        println!("\n{BOLD}==> Entering Bootloader Mode{RESET}");
        println!("{FG_YELLOW}[>] Sending bootloader command over {acm_port}...{RESET}");

        if let Err(e) = send_enter_bootloader(&acm_port) {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Bootloader request failed.");
            eprintln!("    Details: {}", e);
            std::process::exit(EX_UNAVAILABLE);
        }

        println!("{FG_YELLOW}[>] Waiting for bootloader enumeration ({:04X}:{:04X})...{RESET}",
                 NZYME_VID, NZYME_BOOTLOADER_PID);

        // Wait until device enumerates as bootloader
        let start = Instant::now();
        let timeout = Duration::from_secs(10);
        loop {
            match nzyme_device_is_connected(&device_serial, NZYME_VID, NZYME_BOOTLOADER_PID) {
                Ok(true) => {
                    println!("{FG_GREEN}[*] Bootloader mode detected.{RESET}");
                    break;
                }
                Ok(false) => {}
                Err(e) => {
                    eprintln!("    Detection retry: {}", e);
                }
            }

            if start.elapsed() > timeout {
                eprintln!("{FG_RED}[x] ERROR:{RESET} Bootloader did not appear in time.");
                std::process::exit(EX_UNAVAILABLE);
            }

            sleep(Duration::from_millis(500));
        }
    }

    println!("\n{BOLD}==> Flashing Firmware{RESET}");
    println!("{FG_YELLOW}[>] Writing firmware image...{RESET}");

    if let Err(e) = flash_firmware(&acm_port, firmware) {
        eprintln!("{FG_RED}[x] ERROR:{RESET} Firmware flash failed.");
        eprintln!("    Details: {}", e);
        std::process::exit(EX_UNAVAILABLE);
    }

    println!("{FG_GREEN}[*] Firmware written successfully.{RESET}");

    println!("\n{BOLD}==> Waiting for Device Reboot{RESET}");
    println!(
        "{FG_YELLOW}[>] Expecting device as {:04X}:{:04X}...{RESET}",
        device.vid, device.pid
    );

    let start = Instant::now();
    let timeout = Duration::from_secs(30);
    loop {
        match nzyme_device_is_connected(&device_serial, device.vid, device.pid) {
            Ok(true) => {
                println!("{FG_GREEN}[*] Device reconnected in application mode.{RESET}");
                break;
            }
            Ok(false) => {}
            Err(e) => {
                eprintln!("    Detection retry: {}", e);
            }
        }

        if start.elapsed() > timeout {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Device did not reappear in time.");
            std::process::exit(EX_UNAVAILABLE);
        }

        sleep(Duration::from_millis(500));
    }

    println!("\n{BOLD}==> Verifying Firmware Version{RESET}");

    let refreshed_devices = detect_nzyme_usb_devices().unwrap_or_else(|e| {
        eprintln!("{FG_RED}[x] ERROR:{RESET} Could not enumerate devices after flash.");
        eprintln!("    Details: {}", e);
        std::process::exit(EX_UNAVAILABLE);
    });

    let new_device = refreshed_devices
        .into_iter()
        .find(|d| d.serial == device_serial)
        .unwrap_or_else(|| {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Device disappeared after flashing.");
            std::process::exit(EX_UNAVAILABLE);
        });

    println!(
        "{FG_GREEN}[*] New firmware version: v{}.{} {RESET}",
        new_device.firmware_version.major, new_device.firmware_version.minor
    );

    println!("{BOLD}\n==> Firmware Upgrade Completed Successfully{RESET}");
}