use anyhow::{bail, Error};

pub fn cobs_decode(input: &[u8]) -> Result<Vec<u8>, Error> {
    let mut out = Vec::with_capacity(input.len());

    let mut i = 0;
    while i < input.len() {
        let code = input[i];
        if code == 0 {
            bail!("Zero value in COBS data");
        }
        i += 1;

        let block_len = (code as usize) - 1;
        if block_len+i > input.len() {
            // Invalid length. (ran past end)
            bail!("COBS overrun");
        }

        // Copy block_len bytes.
        out.extend_from_slice(&input[i .. i + block_len]);
        i += block_len;

        // Insert an implicit zero between blocks when code != 0xFF.
        if code != 0xFF && i < input.len() {
            out.push(0);
        }
    }

    Ok(out)
}

pub fn cobs_encode(input: &[u8]) -> Vec<u8> {
    let mut out = Vec::with_capacity(input.len() + input.len() / 254 + 2);

    let mut code_index = 0usize;
    out.push(0);
    let mut code: u8 = 1;

    for &b in input {
        if b == 0 {
            out[code_index] = code;
            code_index = out.len();
            out.push(0);
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