use std::fs;
use ed25519_dalek::{Signature, VerifyingKey, Verifier};
use sha2::{Digest, Sha256};
use crate::exit_codes::{EX_IOERR, EX_UNAVAILABLE};
use crate::tools::as_path_buf;

pub fn run(release_file: String, signature_file: String, public_key_file: String) {
    const RESET: &str = "\x1b[0m";
    const BOLD: &str = "\x1b[1m";
    const FG_RED: &str = "\x1b[31m";
    const FG_GREEN: &str = "\x1b[32m";
    const FG_YELLOW: &str = "\x1b[33m";

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

    // Load signature file.
    let signature_bytes = match fs::read(&as_path_buf(&signature_file)) {
        Ok(bytes) => bytes,
        Err(e) => {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Could not load signature file [{}].",
                      signature_file);
            eprintln!("    Details: {}", e);
            std::process::exit(EX_IOERR);
        }
    };

    if signature_bytes.len() != 64 {
        eprintln!("{FG_RED}[x] ERROR:{RESET} Signature file [{}] must be exactly 64 bytes \
            (raw Ed25519 signature).", signature_file);
        eprintln!("    Got: {} bytes", signature_bytes.len());
        std::process::exit(EX_UNAVAILABLE);
    }

    let mut sig_arr = [0u8; 64];
    sig_arr.copy_from_slice(&signature_bytes);
    let signature = Signature::from_bytes(&sig_arr);

    // Load release file.
    let release_bytes = match fs::read(&as_path_buf(&release_file)) {
        Ok(bytes) => bytes,
        Err(e) => {
            eprintln!("{FG_RED}[x] ERROR:{RESET} Could not load release file [{}].",
                      release_file);
            eprintln!("    Details: {}", e);
            std::process::exit(EX_IOERR);
        }
    };

    let release_size = release_bytes.len();

    // Ed25519 verification.
    let verify_result = verifying_key.verify(&release_bytes, &signature);

    println!("{BOLD}==> Nzyme Release Signature Verification{RESET}");
    println!("    Release File  : {}", release_file);
    println!("    Signature File: {}", signature_file);
    println!("    Public Key    : {}", public_key_file);
    println!("    File Size     : {} bytes", release_size);
    println!();

    let release_ok = match verify_result {
        Ok(()) => {
            println!(
                "{FG_GREEN}{BOLD}[✓] Signature VALID{RESET} — release is authentic and unmodified."
            );
            true
        }
        Err(e) => {
            eprintln!(
                "{FG_RED}{BOLD}[x] Signature INVALID{RESET} — release authenticity cannot be \
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

    if !release_ok {
        std::process::exit(EX_UNAVAILABLE);
    }
}

pub fn print_pretty_sha256_hex(data: &[u8]) {
    let mut hasher = Sha256::new();
    hasher.update(data);
    let digest = hasher.finalize();

    let hex = digest.iter().map(|b| format!("{:02x}", b)).collect::<String>();

    for line in hex.as_bytes().chunks(32) {
        let s = std::str::from_utf8(line).unwrap();

        let mut groups = Vec::new();
        for g in s.as_bytes().chunks(8) {
            groups.push(std::str::from_utf8(g).unwrap());
        }

        println!("    {}", groups.join(" "));
    }

    println!();
}