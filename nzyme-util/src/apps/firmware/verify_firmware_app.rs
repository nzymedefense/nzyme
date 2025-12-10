use std::fs;
use ed25519_dalek::{Signature, VerifyingKey};
use ed25519_dalek::DigestVerifier;
use sha2::{Sha512, Digest, Sha256};
use crate::exit_codes::{EX_IOERR, EX_UNAVAILABLE};
use crate::firmware::firmware::{FILE_TYPE_FIRMWARE_BINARY, HEADER_VERSION, NZYME_USB_VENDOR_ID, NZ_MAGIC};
use crate::firmware::firmware_file::load_firmware_file;
use crate::tools::as_path_buf;

pub fn run(firmware_file: String, public_key_file: String) {
    const RESET: &str = "\x1b[0m";
    const BOLD: &str = "\x1b[1m";
    const FG_RED: &str = "\x1b[31m";
    const FG_GREEN: &str = "\x1b[32m";
    const FG_YELLOW: &str = "\x1b[33m";

    let fw = load_firmware_file(firmware_file.clone());

    // Strong semantic checks (fatal if mismatched).
    if fw.magic != NZ_MAGIC {
        eprintln!("{FG_RED}[x] ERROR:{RESET} Invalid magic in firmware file.");
        eprintln!("    Got:   {:02X} {:02X}", fw.magic[0], fw.magic[1]);
        eprintln!("    Expect {:02X} {:02X}", NZ_MAGIC[0], NZ_MAGIC[1]);
        std::process::exit(EX_UNAVAILABLE);
    }

    if fw.header_version != HEADER_VERSION {
        eprintln!("{FG_RED}[x] ERROR:{RESET} Unexpected header version.");
        eprintln!("    Got:   {}", fw.header_version);
        eprintln!("    Expect {}", HEADER_VERSION);
        std::process::exit(EX_UNAVAILABLE);
    }

    if fw.file_type != FILE_TYPE_FIRMWARE_BINARY {
        eprintln!("{FG_RED}[x] ERROR:{RESET} Unexpected file type.");
        eprintln!("    Got:   {}", fw.file_type);
        eprintln!("    Expect {}", FILE_TYPE_FIRMWARE_BINARY);
        std::process::exit(EX_UNAVAILABLE);
    }

    if fw.usb_vid != NZYME_USB_VENDOR_ID {
        eprintln!("{FG_RED}[x] ERROR:{RESET} Unexpected USB Vendor ID.");
        eprintln!("    Got:   0x{:04X}", fw.usb_vid);
        eprintln!("    Expect 0x{:04X}", NZYME_USB_VENDOR_ID);
        std::process::exit(EX_UNAVAILABLE);
    }

    if fw.firmware_len == 0 {
        eprintln!("{FG_RED}[x] ERROR:{RESET} Firmware length is zero.");
        std::process::exit(EX_UNAVAILABLE);
    }

    // Load public key file.
    let pubkey_bytes = match fs::read(&as_path_buf(&public_key_file)) {
        Ok(bytes) => bytes,
        Err(e) => {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Could not load public key file [{}].",
                      public_key_file);
            eprintln!("    Details: {}", e);
            std::process::exit(EX_IOERR);
        }
    };

    // Key file length.
    if pubkey_bytes.len() != 32 {
        eprintln!("{FG_RED}[x] ERROR:{RESET} Public key file [{}] must be exactly 32 bytes \
            (raw Ed25519 pubkey).", public_key_file);
        eprintln!("    Got: {} bytes", pubkey_bytes.len());
        std::process::exit(EX_UNAVAILABLE);
    }

    let mut pk_arr = [0u8; 32];
    pk_arr.copy_from_slice(&pubkey_bytes);
    let verifying_key = match VerifyingKey::from_bytes(&pk_arr) {
        Ok(vk) => vk,
        Err(e) => {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Invalid Ed25519 public key in [{}].",
                      public_key_file);
            eprintln!("    Details: {}", e);
            std::process::exit(EX_UNAVAILABLE);
        }
    };

    // Build signature from header bytes.
    let signature = Signature::from_bytes(&fw.signature);

    // Build hash: SHA-512(header_without_sig || firmware_payload)
    let mut hasher = Sha512::new();
    hasher.update(&fw.header_without_signature);
    hasher.update(&fw.firmware_payload);

    // Ed25519ph verification.
    let verify_result = verifying_key.verify_digest(hasher, &signature);

    println!("{BOLD}==> Nzyme Firmware Signature Verification{RESET}");
    println!("    Firmware File : {}", fw.path);
    println!("    Public Key    : {}", public_key_file);
    println!("    File Size     : {} bytes", fw.total_size);
    println!("    FW Length     : {} bytes", fw.firmware_len);
    println!("    USB VID:PID   : 0x{:04X}:0x{:04X}", fw.usb_vid, fw.usb_pid);
    println!("    FW Version    : {}.{}", fw.fw_major, fw.fw_minor);
    println!("    Signing KeyID : {}", fw.signing_key_id);
    println!();

    let firmware_ok = match verify_result {
        Ok(()) => {
            println!(
                "{FG_GREEN}{BOLD}[✓] Signature VALID{RESET} — firmware is authentic and unmodified."
            );

            true
        }
        Err(e) => {
            eprintln!(
                "{FG_RED}{BOLD}[x] Signature INVALID{RESET} — firmware authenticity cannot be \
                    confirmed."
            );
            eprintln!("    Details: {}", e);

            false
        }
    };

    println!();

    println!("{FG_YELLOW}[!!] Verify supplied public key against trusted source:{RESET}\n");
    print_pretty_sha256_hex(&pk_arr);

    println!("{FG_YELLOW}[!!] Additionally, always verify authenticity of this executable as \
        documented.{RESET}");

    if !firmware_ok {
        std::process::exit(EX_UNAVAILABLE);
    }
}

pub fn print_pretty_sha256_hex(data: &[u8]) {
    let mut hasher = Sha256::new();
    hasher.update(data);
    let digest = hasher.finalize();

    // Convert to lowercase ASCII hex.
    let hex = digest.iter().map(|b| format!("{:02x}", b)).collect::<String>();

    // 4 groups of 8 hex chars = 32 characters per printed line.
    for line in hex.as_bytes().chunks(32) {
        let s = std::str::from_utf8(line).unwrap();

        // Split into groups of 8 hex chars.
        let mut groups = Vec::new();
        for g in s.as_bytes().chunks(8) {
            groups.push(std::str::from_utf8(g).unwrap());
        }

        println!("    {}", groups.join(" "));
    }

    println!();
}
