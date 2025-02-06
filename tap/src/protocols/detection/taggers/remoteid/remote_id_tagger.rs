use log::{error, info};
use crate::tracemark;

pub fn tag(data: &[u8]) -> Option<()> {
    if data.len() < 8 {
        return None
    }

    let message_type = (data[5] & 0xF0) >> 4;
    let protocol_version = data[5] & 0x0F;

    /*
     * We support both of the existing F3411 versions. The original version "19" has not been
     * significantly changed on the protocol level and is backwards-compatible. We do not need
     * to parse it differently.
     */
    if (message_type != 15 && message_type > 5) || (protocol_version != 1 && protocol_version != 2) {
        tracemark!("Not a supported version or not a Remote ID message at all");
        return None
    }
    
    if message_type == 15 {
        // Message Pack. Iterate over all messages.
        let msgpack_size = data[6];
        let msgpack_message_quantity = data[7];

        let mut cursor = 8;

        loop {
            if data.len() < cursor+25 { // Message must be 25 bytes long.
                break
            }

            parse_message(&data[cursor..cursor+25]);

            cursor += 25;
        }

        None
    } else {
        // Single message. Parse.
        if data.len() < 27 {
            tracemark!("Single message too short. Length: {}", data.len());
            return None
        }

        parse_message(&data[5..data.len()])
    }
}

fn parse_message(data: &[u8]) -> Option<()> {
    if data.len() < 3 {
        return None
    }

    let message_type = (data[0] & 0xF0) >> 4;
    let message_payload = &data[1..data.len()];

    match message_type {
        5 => parse_operator_id_message(message_payload),
        _ => None
    }
}

fn parse_operator_id_message(data: &[u8]) -> Option<()> {
    if data.len() < 24 {
        tracemark!("Operator ID message too short. Length: {}", data.len());
        return None
    }

    // Message is 20 bytes padded ASCII.
    let operator_id = ascii_padded_to_string(&data[1..21]);

    info!("Operator ID: {}", operator_id);

    None
}

fn ascii_padded_to_string(buf: &[u8]) -> String {
    // Find the first null byte (if any). If none found, treat the entire buffer as the string.
    let end = buf.iter().position(|&b| b == 0).unwrap_or(buf.len());

    // Slice up to the null terminator (or the entire buffer).
    let text_slice = &buf[..end];

    // Convert tp string. (ASCII is subset of UTF-8)
    match std::str::from_utf8(text_slice) {
        Ok(s) => s.to_string(),
        Err(_) => {
            error!("Could not convert to buffer to string. Returning empty string.");
            "".to_string()
        }
    }
}
