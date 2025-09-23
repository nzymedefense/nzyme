use std::time::Duration;

#[derive(Debug, Clone, Copy)]
struct RxHdr {
    freq_mhz: u16,
    rssi_dbm: Option<f32>,
    rate_flags: u8,
    rate_code: u8,
}

impl RxHdr {
    const BYTES: usize = 6;
    fn parse_le(b: &[u8]) -> Option<Self> {
        if b.len() < Self::BYTES { return None; }

        let rssi_raw = b[2] as i8;

        let rssi_dbm = if rssi_raw < 0 {
            Some(rssi_raw as f32)
        } else {
            // Invalid RSSI.
            None
        };

        Some(Self {
            freq_mhz: u16::from_le_bytes([b[0], b[1]]),
            rssi_dbm,
            rate_flags: b[4],
            rate_code: b[5],
        })
    }
}

/// Simple COBS decoder (for one frame, w/o trailing 0x00).
/// Returns Err if the encoded data is malformed.
fn cobs_decode(input: &[u8]) -> Result<Vec<u8>, &'static str> {
    let mut out = Vec::with_capacity(input.len());
    let mut i = 0;
    while i < input.len() {
        let code = input[i];
        if code == 0 { return Err("zero in COBS data"); }
        i += 1;

        let block_len = (code as usize) - 1;
        if i + block_len > input.len() {
            // invalid length (ran past end)
            return Err("COBS overrun");
        }

        // copy block_len bytes as-is
        out.extend_from_slice(&input[i .. i + block_len]);
        i += block_len;

        // insert an implicit zero between blocks when code != 0xFF
        if code != 0xFF && i < input.len() {
            out.push(0);
        }
    }
    Ok(out)
}

pub fn run() -> anyhow::Result<()> {
    // Open CDC-ACM (baud is ignored by host, but must set something)
    let port_name = "/dev/ttyACM4";
    let mut port = serialport::new(port_name, 115_200)
        .timeout(Duration::from_millis(100))
        .open()?;
    println!("Opened {}", port_name);

    // Chunk buffer from OS + stream accumulator
    let mut chunk = [0u8; 512];
    let mut acc: Vec<u8> = Vec::with_capacity(8192);

    loop {
        match port.read(&mut chunk) {
            Ok(n) if n > 0 => {
                acc.extend_from_slice(&chunk[..n]);

                while let Some(pos) = acc.iter().position(|&b| b == 0) {
                        let encoded = acc[..pos].to_vec();     // one encoded packet (without 0x00)
                        acc.drain(0..=pos);                    // drop encoded + delimiter

                        if encoded.is_empty() {
                            // empty frame (can happen on line noise) -> skip
                            continue;
                        }

                        match cobs_decode(&encoded) {
                            Ok(decoded) => {
                                // decoded = [raw_rx_pkt_header (6B) || 802.11 frame ...]
                                if decoded.len() < RxHdr::BYTES {
                                    eprintln!("short decoded frame ({} bytes), dropping", decoded.len());
                                    continue;
                                }
                                let hdr = RxHdr::parse_le(&decoded[..RxHdr::BYTES]).unwrap();
                                let frame = &decoded[RxHdr::BYTES..];

                                println!(
                                    "freq={} MHz  rssi={:?} dBm  flags=0x{:02x} rate=0x{:02x}  len={}",
                                    hdr.freq_mhz, hdr.rssi_dbm, hdr.rate_flags, hdr.rate_code, frame.len()
                                );
                                // Hex preview of first bytes
                                let show = frame.len().min(32);
                                print!("frame[0..{}]:", show);
                                for b in &frame[..show] { print!(" {:02x}", b); }
                                println!();
                                if frame.len() > show {
                                    println!("… ({} more bytes)\n", frame.len() - show);
                                }
                            }
                            Err(e) => {
                                eprintln!("COBS decode error: {e} (dropping until next delimiter)");
                                // carry on; next 0x00 will re-sync
                            }
                        }
                }
            }
            Ok(_) => {} // zero bytes this read
            Err(ref e) if e.kind() == std::io::ErrorKind::TimedOut => {
                // nothing within timeout; loop again
            }
            Err(e) => {
                eprintln!("serial read error: {e}");
            }
        }
    }
}