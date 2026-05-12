use crate::exit_codes::{EX_PERMISSION_DENIED, EX_UNAVAILABLE};
use crate::usb::usb::{detect_nzyme_usb_devices, SONA_WIFI_PID};

const DEVICES_PER_SONA: usize = 4;
const MAX_SONAS: usize = 4;

const CHANNELS_2G: &[u16] = &[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14];
const CHANNELS_5G: &[u16] = &[
    36, 40, 44, 48, 52, 56, 60, 64,
    100, 104, 108, 112, 116, 120, 124,
    128, 132, 136, 140, 144,
    149, 153, 157, 161, 165, 169, 173, 177,
];

pub fn run() {
    const RESET: &str = "\x1b[0m";
    const BOLD: &str = "\x1b[1m";
    const FG_RED: &str = "\x1b[31m";
    const FG_GREEN: &str = "\x1b[32m";
    const FG_YELLOW: &str = "\x1b[33m";
    const FG_CYAN: &str = "\x1b[36m";

    eprintln!("{BOLD}==> Sona Configuration Generator{RESET}");
    eprintln!("    Generating configuration for all connected Sona devices.\n");

    eprintln!("{FG_YELLOW}[>] Detecting connected Sona devices...{RESET}");

    let devices = match detect_nzyme_usb_devices() {
        Ok(devices) => devices.into_iter()
            .filter(|d| d.pid == SONA_WIFI_PID)
            .collect::<Vec<_>>(),
        Err(e) => {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Could not detect Nzyme USB devices.");
            eprintln!("    Details: {}", e);
            std::process::exit(EX_PERMISSION_DENIED);
        }
    };

    let sona_count = devices.len() / DEVICES_PER_SONA;
    if devices.len() % DEVICES_PER_SONA != 0 || !(1..=MAX_SONAS).contains(&sona_count) {
        eprintln!("{FG_RED}[x] ERROR:{RESET} Expected 1-{} connected Sonas ({} interfaces \
            each). Found {} interfaces.", MAX_SONAS, DEVICES_PER_SONA, devices.len());
        std::process::exit(EX_UNAVAILABLE);
    }

    eprintln!("{FG_GREEN}[*] Found {} Sona ({} interfaces).{RESET}\n",
              sona_count, devices.len());

    eprintln!("{FG_CYAN}Detected Interfaces:{RESET}");
    for (i, device) in devices.iter().enumerate() {
        eprintln!("    [{}] sona-{}", i + 1, device.serial);
    }
    eprintln!();

    // Split devices ~in half: the first half covers 2.4 GHz, the second half covers 5 GHz.
    let devices_2g_count = devices.len() / 2;
    let devices_5g_count = devices.len() - devices_2g_count;
    let split_2g = split_evenly(CHANNELS_2G, devices_2g_count);
    let split_5g = split_evenly(CHANNELS_5G, devices_5g_count);

    eprintln!("{BOLD}==> Generated Configuration{RESET}");
    eprintln!("    Copy the block between the markers below into your nzyme tap");
    eprintln!("    configuration file. Channels are split evenly across all interfaces.\n");

    eprintln!("{FG_YELLOW}---------- BEGIN CONFIGURATION ----------{RESET}\n");

    // Actual config to stdout. No colors, clean for piping or copying.
    for (i, device) in devices.iter().enumerate() {
        let (channels_2g, channels_5g): (&[u16], &[u16]) = if i < devices_2g_count {
            (&split_2g[i], &[])
        } else {
            (&[], &split_5g[i - devices_2g_count])
        };

        println!("[wifi_interfaces.sona-{}]", device.serial);
        println!("active = true");
        println!("channels_2g = {}", format_channels(channels_2g));
        println!("channels_5g = {}", format_channels(channels_5g));
        println!();
    }

    eprintln!("{FG_YELLOW}----------- END CONFIGURATION -----------{RESET}\n");

    eprintln!("{FG_GREEN}[*] Configuration generated for {} interfaces.{RESET}",
              devices.len());
}

fn split_evenly(items: &[u16], buckets: usize) -> Vec<&[u16]> {
    if buckets == 0 {
        return Vec::new();
    }
    let base = items.len() / buckets;
    let extra = items.len() % buckets;
    let mut out = Vec::with_capacity(buckets);
    let mut start = 0;
    for i in 0..buckets {
        let len = base + if i < extra { 1 } else { 0 };
        out.push(&items[start..start + len]);
        start += len;
    }
    out
}

fn format_channels(channels: &[u16]) -> String {
    let inner: Vec<String> = channels.iter().map(|c| c.to_string()).collect();
    format!("[{}]", inner.join(", "))
}