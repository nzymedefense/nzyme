use std::collections::BTreeMap;
use anyhow::{anyhow, bail, Context, Error, Result};
use std::io::Write;
use std::time::{Duration, Instant};
use base64::Engine;
use serde_cbor::Value;
use serialport::SerialPort;
use sha2::{Digest, Sha256};

// Nzyme Bootloader Protocol.
const MSG_TYPE_CMD: u8 = 2;
const PROTO_VERSION: u8 = 1;
const CMD_ENTER_BOOTLOADER: u8 = 2;

// Zephyr / MCUboot protocol.
const OS_MGMT_GROUP: u16 = 0;
const OS_MGMT_ID_RESET: u8 = 5;
const MGMT_OP_WRITE: u8 = 2;
const MGMT_FLAGS: u8 = 0;
const NLIP_PKT_START1: u8 = 0x06;
const NLIP_PKT_START2: u8 = 0x09;
const NLIP_END: u8 = 0x0A;
const NLIP_CONT1: u8 = 0x04;
const NLIP_CONT2: u8 = 0x14;
const IMG_MGMT_GROUP: u16 = 1;
const IMG_MGMT_ID_UPLOAD: u8 = 1;
const MAX_FRAME_LEN: usize = 127;
const MAX_PAYLOAD_PER_FRAME: usize = MAX_FRAME_LEN - 2 - 1; // marker + '\n'

fn cobs_encode(input: &[u8]) -> Vec<u8> {
    let mut out: Vec<u8> = Vec::with_capacity(input.len() + input.len() / 254 + 2);

    let mut code_index = 0usize;
    out.push(0); // Placeholder for code byte.
    let mut code: u8 = 1;

    for &b in input {
        if b == 0 {
            out[code_index] = code;
            code_index = out.len();
            out.push(0); // Next code placeholder.
            code = 1;
        } else {
            out.push(b);
            code = code.wrapping_add(1);
            if code == 0xFF {
                out[code_index] = code;
                code_index = out.len();
                out.push(0);
                code = 1;
            }
        }
    }

    out[code_index] = code;
    out
}

pub fn send_enter_bootloader<P: AsRef<std::path::Path>>(dev_path: P) -> Result<()> {
    let dev_str = dev_path
        .as_ref()
        .to_str()
        .context("Device path is not valid UTF-8")?
        .to_string();

    let mut port = serialport::new(&dev_str, 115_200)
        .timeout(Duration::from_secs(1))
        .open()
        .with_context(|| format!("Could not open [{}].", dev_str))?;

    // Payload is only the command id.
    let payload = [CMD_ENTER_BOOTLOADER];
    let payload_len = payload.len() as u16;

    // Build message header (type, version, len).
    let mut raw: [u8; 5] = [0; 5];
    raw[0] = MSG_TYPE_CMD;
    raw[1] = PROTO_VERSION;
    raw[2] = (payload_len & 0x00FF) as u8;
    raw[3] = ((payload_len >> 8) & 0x00FF) as u8;
    raw[4] = payload[0];

    // COBS frame + delimiter.
    let enc = cobs_encode(&raw);

    port.write_all(&enc)
        .context("Could not write enter-bootloader COBS frame.")?;
    port.write_all(&[0x00])
        .context("Could not write COBS delimiter.")?;

    // Best-effort flush. (device reboots immediately after parsing)
    let _ = port.flush();

    Ok(())
}

pub fn flash_firmware(acm_port: &str, firmware: Vec<u8>) -> Result<(), Error> {
    let mut port = serialport::new(acm_port, 115_200)
        .timeout(Duration::from_millis(200))
        .open()
        .with_context(|| format!("Could not open [{}].", acm_port))?;

    let total_len_u64 = firmware.len() as u64;

    let sha: Vec<u8> = {
        let mut hasher = Sha256::new();
        hasher.update(&firmware);
        hasher.finalize().to_vec()
    };

    const CHUNK_SIZE: usize = 128;
    let mut seq: u8 = 1;
    let mut off: u64 = 0;

    // Used to avoid infinite loops if device responds weirdly.
    let mut last_off: Option<u64> = None;

    while off < total_len_u64 {
        if last_off == Some(off) {
            /* We didn’t make progress last iteration; this usually means we mis-parsed
             * the response or the device is rejecting the chunk silently.
             * Continue anyway, but guard against a tight loop.
            */
        }
        last_off = Some(off);

        let end = ((off as usize) + CHUNK_SIZE).min(firmware.len());
        let data = &firmware[off as usize..end];

        // Build CBOR map for this chunk.
        let cbor = build_img_upload_cbor(
            off,
            data,
            if off == 0 { Some(total_len_u64) } else { None },
            if off == 0 { Some(&sha) } else { None }
        )?;

        // Build SMP request.
        let smp = build_smp_v1(
            MGMT_OP_WRITE,
            MGMT_FLAGS,
            IMG_MGMT_GROUP,
            seq,
            IMG_MGMT_ID_UPLOAD,
            &cbor
        );
        let frame = encode_smp_serial_frame(&smp);

        // Write and read response with retry.
        let mut attempt = 0;
        let resp_smp = loop {
            attempt += 1;

            port.write_all(&frame).context("Could not write image upload frame.")?;
            port.flush().ok();

            match read_one_smp_response(&mut *port, Duration::from_secs(3)) {
                Ok(Some(resp)) => break resp,
                Ok(None) => {
                    if attempt >= 3 {
                        bail!("Timeout waiting for image upload response at off=<{}>", off);
                    }

                    // Brief backoff then retry same chunk.
                    std::thread::sleep(Duration::from_millis(150));
                    continue;
                }
                Err(e) => {
                    if attempt >= 3 {
                        return Err(e);
                    }

                    std::thread::sleep(Duration::from_millis(150));
                    continue;
                }
            }
        };

        // Validate response header and parse CBOR.
        if resp_smp.len() < 8 {
            bail!("Image upload response too short.");
        }

        let resp_len = u16::from_be_bytes([resp_smp[2], resp_smp[3]]) as usize;
        let resp_group = u16::from_be_bytes([resp_smp[4], resp_smp[5]]);
        let resp_id = resp_smp[7];

        if resp_group != IMG_MGMT_GROUP || resp_id != IMG_MGMT_ID_UPLOAD {
            bail!("Unexpected SMP response: group=<{}> id=<{}>", resp_group, resp_id);
        }

        let payload = resp_smp
            .get(8..8 + resp_len)
            .ok_or_else(|| anyhow!("SMP response payload length mismatch."))?;

        let (rc, next_off) = parse_img_upload_response(payload)
            .context("Could not parse image upload response.")?;

        if rc != 0 {
            bail!("Device returned rc=<{}> during upload at off=<{}>.", rc, off);
        }

        if next_off < off {
            bail!("Device returned decreasing off: <{}> -> <{}>", off, next_off);
        }

        if next_off == off {
            // No progress; treat as error to avoid infinite loop.
            bail!("Device reported no progress at off=<{}>.", off);
        }

        off = next_off;
        seq = seq.wrapping_add(1);
    }

    Ok(())
}

fn build_smp_v1(op: u8, flags: u8, group: u16, seq: u8, id: u8, payload: &[u8]) -> Vec<u8> {
    let len = payload.len() as u16;

    let mut out = Vec::with_capacity(8 + payload.len());
    out.push(op);
    out.push(flags);
    out.extend_from_slice(&len.to_be_bytes());
    out.extend_from_slice(&group.to_be_bytes());
    out.push(seq);
    out.push(id);
    out.extend_from_slice(payload);

    out
}

fn build_img_upload_cbor(off: u64, data: &[u8], len_opt: Option<u64>, sha_opt: Option<&[u8]>,
    ) -> Result<Vec<u8>> {

    /*
     * CBOR map for MCUboot image upload:
     * { "off": uint, "data": bstr, optional "len": uint, optional "sha": bstr }
     */

    if let Some(sha) = sha_opt {
        if sha.len() != 32 {
            return Err(anyhow!("SHA256 must be 32 bytes long but we got <{}> bytes.", sha.len()));
        }
    }

    let mut map: BTreeMap<Value, Value> = BTreeMap::new();

    // Optional fields first. (BTreeMap will sort keys anyway)
    if let Some(len) = len_opt {
        map.insert(Value::Text("len".to_string()), Value::Integer(len as i128));
    }

    if let Some(sha) = sha_opt {
        map.insert(Value::Text("sha".to_string()), Value::Bytes(sha.to_vec()));
    }

    // Required fields.
    map.insert(Value::Text("off".to_string()), Value::Integer(off as i128));
    map.insert(Value::Text("data".to_string()), Value::Bytes(data.to_vec()));

    Ok(serde_cbor::to_vec(&Value::Map(map))?)
}

fn parse_img_upload_response(payload: &[u8]) -> Result<(i64, u64)> {
    use serde_cbor::Value;

    let v: Value = serde_cbor::from_slice(payload)?;
    let map = match v {
        Value::Map(m) => m,
        _ => bail!("Image upload response is not a CBOR map."),
    };

    let mut rc: Option<i64> = None;
    let mut off: Option<u64> = None;

    for (k, val) in map {
        let key = match k {
            Value::Text(s) => s,
            _ => continue,
        };

        match key.as_str() {
            "rc" => {
                if let Value::Integer(n) = val {
                    let rc_i64: i64 = n.try_into()
                        .map_err(|_| anyhow!("RC does not fit into i64, rc=<{}>", n))?;
                    rc = Some(rc_i64);
                }
            }
            "off" => {
                if let Value::Integer(n) = val {
                    let off_u64: u64 = n.try_into()
                        .map_err(|_| anyhow!("OFF does not fit into u64, off=<{}>", n))?;
                    off = Some(off_u64);
                }
            }
            _ => {}
        }
    }

    let rc = rc.ok_or_else(|| anyhow!("Missing rc in response."))?;
    let off = off.ok_or_else(|| anyhow!("Missing off in response."))?;
    Ok((rc, off))
}

pub fn send_reset(acm_port: &str) -> Result<()> {
    let mut port = serialport::new(acm_port, 115_200)
        .timeout(Duration::from_millis(200))
        .open()
        .with_context(|| format!("Could not open [{}].", acm_port))?;

    let seq = 1u8;

    // Build an SMPv1 OS reset request with empty payload.
    let smp = build_os_reset_smp_v1(seq);

    // Encode as NLIP.
    let frame = encode_smp_serial_frame(&smp);

    port.write_all(&frame).context("Could not write SMP reset frame.")?;
    port.flush().ok();

    // Try to read a response.
    match read_one_smp_response(&mut *port, Duration::from_secs(2))? {
        Some(resp_smp) => {
            // Validate response is for OS group/reset id.
            if resp_smp.len() < 8 {
                return Err(anyhow!("SMP response too short."));
            }

            let resp_op = resp_smp[0];
            let resp_len = u16::from_be_bytes([resp_smp[2], resp_smp[3]]) as usize;
            let resp_group = u16::from_be_bytes([resp_smp[4], resp_smp[5]]);
            let resp_id = resp_smp[7];

            // Response payload begins at offset 8.
            let payload = resp_smp.get(8..8 + resp_len).unwrap_or(&[]);

            // Many mcumgr responses include CBOR {"rc": <int>} on error.
            // If absent, assume success.
            if let Some(rc) = extract_rc_from_cbor(payload)? {
                if rc != 0 {
                    bail!(
                        "Device returned non-zero rc=<{}> (op=<{}>, group=<{}>, id=<{}>)",
                        rc,
                        resp_op,
                        resp_group,
                        resp_id
                    );
                }
            }

            Ok(())
        }
        None => {
            // No response observed. (likely reset happened quickly, and we assume that is fine)
            Ok(())
        }
    }
}

fn build_os_reset_smp_v1(seq: u8) -> Vec<u8> {
    // v1 SMP header is 8 bytes: op, flags, len_be16, group_be16, seq, id
    let len: u16 = 0; // EMPTY payload

    let mut out = Vec::with_capacity(8);
    out.push(MGMT_OP_WRITE);
    out.push(MGMT_FLAGS);
    out.extend_from_slice(&len.to_be_bytes());
    out.extend_from_slice(&OS_MGMT_GROUP.to_be_bytes());
    out.push(seq);
    out.push(OS_MGMT_ID_RESET);
    out
}

fn encode_smp_serial_frame(smp_packet: &[u8]) -> Vec<u8> {
    let crc = crc16_ccitt_init0(smp_packet);

    // Total length = smp_packet + crc16. (not counting the total_len field)
    let total_len = (smp_packet.len() as u16) + 2;

    let mut inner = Vec::with_capacity(2 + smp_packet.len() + 2);
    inner.extend_from_slice(&total_len.to_be_bytes());
    inner.extend_from_slice(smp_packet);
    inner.extend_from_slice(&crc.to_be_bytes());

    let b64 = base64::engine::general_purpose::STANDARD.encode(&inner);
    let b64_bytes = b64.as_bytes();

    /*
     * Fragment across multiple NLIP frames:
     * first: 0x06 0x09
     * cont : 0x04 0x14
     * each ends with '\n'
     */
    let mut out = Vec::with_capacity(
        2 + b64_bytes.len() + (b64_bytes.len() / MAX_PAYLOAD_PER_FRAME + 1) * 3
    );

    let mut offset = 0usize;
    let mut first = true;

    while offset < b64_bytes.len() {
        let end = (offset + MAX_PAYLOAD_PER_FRAME).min(b64_bytes.len());
        let chunk = &b64_bytes[offset..end];

        if first {
            out.push(NLIP_PKT_START1);
            out.push(NLIP_PKT_START2);
            first = false;
        } else {
            out.push(NLIP_CONT1);
            out.push(NLIP_CONT2);
        }

        out.extend_from_slice(chunk);
        out.push(NLIP_END);

        offset = end;
    }

    out
}

fn read_one_smp_response(port: &mut dyn SerialPort, timeout: Duration) -> Result<Option<Vec<u8>>> {
    let start = Instant::now();

    // Accumulate base64.
    let mut b64_accum: Vec<u8> = Vec::new();

    loop {
        if start.elapsed() > timeout {
            return Ok(None);
        }

        // Find next frame marker. (start or continuation)
        let (m1, m2) = match read_marker(port, Duration::from_millis(200)) {
            Ok(Some(m)) => m,
            Ok(None) => continue,
            Err(e) => return Err(e),
        };

        let is_start = m1 == NLIP_PKT_START1 && m2 == NLIP_PKT_START2;
        let is_cont  = m1 == NLIP_CONT1 && m2 == NLIP_CONT2;

        if !is_start && !is_cont {
            // Ignore noise.
            continue;
        }

        if is_start {
            b64_accum.clear();
        } else if is_cont && b64_accum.is_empty() {
            // Continuation without start. Ignore.
            continue;
        }

        // Read base64 until newline.
        let line = read_until_newline(port, timeout.saturating_sub(start.elapsed()))?;
        b64_accum.extend_from_slice(&line);

        /*
         * We don't know if more continuations are coming; try to decode and validate now.
         * If decode fails due to incomplete data, continue reading more frames.
         */
        match try_decode_and_validate_smp(&b64_accum) {
            Ok(Some(smp)) => return Ok(Some(smp)),
            Ok(None) => continue, // We need more data.
            Err(e) => return Err(e),
        }
    }
}

fn read_marker(port: &mut dyn SerialPort, timeout: Duration) -> Result<Option<(u8, u8)>> {
    let start = Instant::now();
    let mut b1 = [0u8; 1];
    let mut b2 = [0u8; 1];

    while start.elapsed() < timeout {
        match port.read_exact(&mut b1) {
            Ok(()) => {}
            Err(e) if e.kind() == std::io::ErrorKind::TimedOut => continue,
            Err(e) => return Err(e.into()),
        }

        match port.read_exact(&mut b2) {
            Ok(()) => return Ok(Some((b1[0], b2[0]))),
            Err(e) if e.kind() == std::io::ErrorKind::TimedOut => continue,
            Err(e) => return Err(e.into()),
        }
    }

    Ok(None)
}

fn read_until_newline(port: &mut dyn SerialPort, timeout: Duration) -> Result<Vec<u8>> {
    let start = Instant::now();
    let mut out = Vec::new();

    loop {
        if start.elapsed() > timeout {
            bail!("Timeout reading NLIP line.");
        }

        let mut one = [0u8; 1];
        match port.read_exact(&mut one) {
            Ok(()) => {
                if one[0] == NLIP_END {
                    return Ok(out);
                }
                out.push(one[0]);
            }
            Err(e) if e.kind() == std::io::ErrorKind::TimedOut => continue,
            Err(e) => return Err(e.into()),
        }
    }
}

fn try_decode_and_validate_smp(b64_accum: &[u8]) -> Result<Option<Vec<u8>>> {
    // If base64 is incomplete, decode will fail. Treat as "need more".
    let decoded = match base64::engine::general_purpose::STANDARD.decode(b64_accum) {
        Ok(d) => d,
        Err(_) => return Ok(None),
    };

    // total_len_be16 || smp_packet || crc16
    if decoded.len() < 2 + 8 + 2 {
        return Ok(None);
    }

    let total_len = u16::from_be_bytes([decoded[0], decoded[1]]) as usize;
    if decoded.len() != 2 + total_len {
        // Likely incomplete. Treat as "need more".
        return Ok(None);
    }

    let smp_len = total_len - 2;
    let smp_packet = &decoded[2..2 + smp_len];

    let crc_recv = u16::from_be_bytes([decoded[2 + smp_len], decoded[2 + smp_len + 1]]);
    let crc_calc = crc16_ccitt_init0(smp_packet);

    if crc_recv != crc_calc {
        bail!("CRC mismatch in response.");
    }

    Ok(Some(smp_packet.to_vec()))
}

fn crc16_ccitt_init0(data: &[u8]) -> u16 {
    let mut crc: u16 = 0x0000;

    for &byte in data {
        crc ^= (byte as u16) << 8;
        for _ in 0..8 {
            if (crc & 0x8000) != 0 {
                crc = (crc << 1) ^ 0x1021;
            } else {
                crc <<= 1;
            }
        }
    }

    crc
}

fn extract_rc_from_cbor(payload: &[u8]) -> Result<Option<i64>> {
    if payload.is_empty() {
        return Ok(None);
    }

    let v: Value = serde_cbor::from_slice(payload)?;

    if let Value::Map(map) = v {
        for (k, val) in map {
            if let Value::Text(s) = k {
                if s == "rc" {
                    if let Value::Integer(n) = val {
                        let rc: i64 = n
                            .try_into()
                            .map_err(|_| anyhow!("CBOR rc does not fit into i64 (rc=<{}>)", n))?;
                        return Ok(Some(rc));
                    }
                    return Ok(None);
                }
            }
        }
    }

    Ok(None)
}
