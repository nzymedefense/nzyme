use std::thread::sleep;
use std::time::{Duration, Instant};
use crate::exit_codes::{EX_PERMISSION_DENIED, EX_UNAVAILABLE};
use crate::firmware::firmware_file::load_firmware_file;
use crate::usb::bootloader::{flash_firmware, send_enter_bootloader};
use crate::usb::mcuboot;
use crate::usb::usb::{detect_nzyme_usb_devices, find_bootloader_of_pid, is_sona, nzyme_device_is_connected, NZYME_VID};

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

    if device.vid != NZYME_VID {
        eprintln!("{FG_RED}[x] ERROR:{RESET} Device is not an Nzyme device.");
        std::process::exit(EX_UNAVAILABLE);
    }

    /*
     * Sona boards use MCUboot and need a different flashing and reset process. We are using MCUboot
     * instead of our own bootloader (like on STM32) because Zephyr pretty much forces you to use
     * it. The goal is to keep both bootloader types as aligned as possible, sometimes at the
     * cost of efficiency, to keep the process simpler.
     */
    let device_bootloader_pid = match find_bootloader_of_pid(device.pid) {
        Ok(p) => p,
        Err(e) => {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Unknown PID <0x{:04X}>.", device.pid);
            std::process::exit(EX_UNAVAILABLE);
        }
    };

    let is_sona = is_sona(device.pid);

    println!("{FG_CYAN}Device Information:{RESET}");
    println!("    Product        : {}", device.product);
    println!("    Manufacturer   : {}", device.manufacturer);
    println!("    Serial Number  : {}", device.serial);
    println!("    Bootloader     : {}", if is_sona { "Sona" } else { "Default" });

    println!("    Firmware       : v{}.{}",
             device.firmware_version.major, device.firmware_version.minor);
    println!("    USB VID:PID    : 0x{:04X}:0x{:04X}", device.vid, device.pid);

    let acm_port = match device.acm_port {
        Some(port) => port,
        None => {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Device exposes no ACM port.");
            std::process::exit(EX_UNAVAILABLE);
        }
    };

    println!("    ACM Port       : {acm_port}\n");

    println!("{BOLD}==> Loading Firmware{RESET}");
    let firmware = load_firmware_file(firmware_file);

    /*
     * Check that the firmware is intended for this device. We will check for the device PID
     * as well as the PID of the bootloader of the device.
     */
    if device.pid != firmware.usb_pid && device.pid != device_bootloader_pid {
        eprintln!("{FG_RED}[x] ERROR:{RESET} Firmware PID <0x{:04X}> is not matching device \
            PID <0x{:04X}/0x{:04X}>. Make sure the firmware your are trying to load is intended for \
            the target device.", firmware.usb_pid, device.pid, device_bootloader_pid);
        std::process::exit(EX_UNAVAILABLE);
    }

    if device.pid == device_bootloader_pid {
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

        if is_sona {
            if let Err(e) = mcuboot::send_enter_bootloader(&acm_port) {
                eprintln!("{FG_RED}[x] ERROR:{RESET} Bootloader request failed.");
                eprintln!("    Details: {}", e);
                std::process::exit(EX_UNAVAILABLE);
            }
        } else {
            if let Err(e) = send_enter_bootloader(&acm_port) {
                eprintln!("{FG_RED}[x] ERROR:{RESET} Bootloader request failed.");
                eprintln!("    Details: {}", e);
                std::process::exit(EX_UNAVAILABLE);
            }
        }

        println!("{FG_YELLOW}[>] Waiting for bootloader enumeration (0x{:04X}:0x{:04X})...{RESET}",
                 NZYME_VID, device_bootloader_pid);

        // Wait until device enumerates as bootloader
        let start = Instant::now();
        let timeout = Duration::from_secs(10);

        loop {
            sleep(Duration::from_millis(500));
            
            match nzyme_device_is_connected(&device_serial, NZYME_VID, device_bootloader_pid) {
                Ok(true) => {
                    println!("{FG_GREEN}[*] Bootloader mode detected.{RESET}");
                    break;
                }
                Ok(false) => {}
                Err(e) => { /* Ignore. */ }
            }

            if start.elapsed() > timeout {
                eprintln!("{FG_RED}[x] ERROR:{RESET} Bootloader did not appear in time.");
                std::process::exit(EX_UNAVAILABLE);
            }
        }
    }

    // Detect ACM port again. It may have changed for bootloader.
    let devices = match detect_nzyme_usb_devices() {
        Ok(devices) => devices,
        Err(e) => {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Could not detect Nzyme USB devices.");
            eprintln!("    Details: {}", e);
            std::process::exit(EX_PERMISSION_DENIED);
        }
    };

    let bootloader_device = match devices.into_iter()
            .find(|d| d.serial == device_serial && d.vid == NZYME_VID && d.pid == device_bootloader_pid) {
        Some(d) => d,
        None => {
            eprintln!("{FG_RED}[x] ERROR:{RESET} No device with serial [{device_serial}] found.");
            std::process::exit(EX_UNAVAILABLE);
        }
    };
    let bl_acm_port = match bootloader_device.acm_port {
        Some(port) => port,
        None => {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Device exposes no ACM port.");
            std::process::exit(EX_UNAVAILABLE);
        }
    };

    println!("\n{BOLD}==> Flashing Firmware{RESET}");
    println!("{FG_YELLOW}[>] Writing firmware image...{RESET}");

    if is_sona {
        // Cut off Nzyme file header and flash the raw image. Bootloader validates.
        if let Err(e) = mcuboot::flash_firmware(&bl_acm_port, firmware.firmware_payload) {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Firmware flash failed.");
            eprintln!("    Details: {}", e);
            std::process::exit(EX_UNAVAILABLE);
        }

        // MCUboot needs an explicit reset command.
        sleep(Duration::from_secs(1));
        if let Err(e) = mcuboot::send_reset(&bl_acm_port) {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Bootloader reset failed.");
            eprintln!("    Details: {}", e);
            std::process::exit(EX_UNAVAILABLE);
        }
    } else {
        if let Err(e) = flash_firmware(&bl_acm_port, firmware.firmware_payload) {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Firmware flash failed.");
            eprintln!("    Details: {}", e);
            std::process::exit(EX_UNAVAILABLE);
        }
    }

    println!("{FG_GREEN}[*] All bytes written. Device is validating and rebooting.{RESET}");

    println!("\n{BOLD}==> Waiting for Device Reboot{RESET}");
    println!(
        "{FG_YELLOW}[>] Expecting device as 0x{:04X}:0x{:04X}...{RESET}",
        device.vid, device.pid
    );

    let start = Instant::now();
    let timeout = Duration::from_secs(30);
    loop {
        match nzyme_device_is_connected(&device_serial, device.vid, device.pid) {
            Ok(true) => {
                println!("{FG_GREEN}[*] Device reconnected.{RESET}");
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

    println!("{BOLD}\n==> Process complete.{RESET}");
}