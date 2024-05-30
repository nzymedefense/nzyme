use anyhow::{bail, Error};
use byteorder::{BigEndian, ByteOrder};
use log::{trace};
use crate::data::tcp_table::TcpSession;
use crate::ethernet::packets::{SocksTunnel, SshSession, SshVersion};
use crate::ethernet::tcp_tools::determine_tcp_session_state;
use crate::tracemark;

const SSH_GREETING: &[u8] = b"SSH-2.0-";

/*
 * We are parsing up to and including the key exchange init to get a strong level of confidence
 * that we are looking at a SSH handshake. Everything after that is not of interest, does not add
 * to such confidence and is also very complex to parse because it's different, depending on
 * authentication method.
 */
pub fn tag(cts: &[u8], stc: &[u8], session: &TcpSession) -> Option<SshSession> {
    if cts.len() < 11 || stc.len() < 11 {
        return None;
    }

    if cts.starts_with(SSH_GREETING) && stc.starts_with(SSH_GREETING) {
        let (mut cts_cursor, client_version) = match parse_version_string(cts) {
            Ok(version) => version,
            Err(e) => {
                tracemark!("SSH: {}", e);
                return None;
            }
        };

        let (mut stc_cursor, server_version) = match parse_version_string(stc) {
            Ok(version) => version,
            Err(e) => {
                tracemark!("SSH: {}", e);
                return None;
            }
        };

        if cts.len() < cts_cursor+4 || stc.len() < stc_cursor+4  {
            tracemark!("SSH");
            return None;
        }

        let client_kexi_len = BigEndian::read_u32(&cts[cts_cursor..cts_cursor+4]) as usize;
        let server_kexi_len = BigEndian::read_u32(&stc[stc_cursor..stc_cursor+4]) as usize;
        cts_cursor += 4;
        stc_cursor += 4;

        if let Err(e) = identify_kex_init(
            &cts[cts_cursor..cts_cursor+client_kexi_len],
            client_kexi_len) {

            tracemark!("SSH: {}", e);
            return None;
        }

        if let Err(e) = identify_kex_init(
            &stc[stc_cursor..stc_cursor+server_kexi_len],
            server_kexi_len) {

            tracemark!("SSH: {}", e);
            return None;
        }

        let (connection_status, terminated_at) = determine_tcp_session_state(session);

        return Some(SshSession {
            client_version,
            server_version,
            connection_status,
            tunneled_bytes: session.bytes_count,
            source_mac: session.source_mac.clone(),
            destination_mac: session.destination_mac.clone(),
            source_address: session.source_address,
            destination_address: session.destination_address,
            source_port: session.source_port,
            destination_port: session.destination_port,
            established_at: session.start_time,
            terminated_at,
            most_recent_segment_time: session.most_recent_segment_time
        });
    }

    tracemark!("SSH");
    None
}

fn parse_version_string(data: &[u8]) -> Result<(usize, SshVersion), Error> {
    /*
     * Find EOL. The standard asks for \CRLF but there may also only be a \LF. We will also
     * take the safe route and expect just a \CR as well.
     *
     * To keep it extra easy, we'll also just cut a potential \CR or \LF off the last parsed
     * string at the end. This way we deal with 1 and 2 control characters for EOL.
     */
    let (eol_pos, skip_eol) = match data.iter().position(|&b| b == 0x0D) {
        Some(pos) => {
            // We have a \CR. Is it followed by an \LF?
            if data.len() < pos+1 {
                bail!("Payload too short.")
            }

            if data[pos+1] != 0x0A {
                // Not followed by \LF. Fine, we'll take the \CR as EOL character.
                (pos, 0)
            } else {
                // Followed by \CR. We have \CRLF and return the \LF as EOL character.
                (pos+1, 1)
            }
        },
        None => {
            // No CR at all. Do we have a LF?
            match data.iter().position(|&b| b == 0x0A) {
                Some(pos) => (pos, 0),
                None => bail!("No CR or LF control characters. No EOL.")
            }
        }
    };

    let mut cursor = 4;
    let dash2_pos = match data[cursor..].iter().position(|&b| b == b'-') {
        Some(pos) => pos,
        None => bail!("No second dash character.")
    };

    // At least one char and \CRLF.
    if data.len() < dash2_pos+3 {
        bail!("Payload too short.")
    }

    let version = String::from_utf8_lossy(&data[cursor..cursor+dash2_pos]).trim().to_string();
    cursor += dash2_pos+1;

    let space_pos = data[cursor..].iter()
        .position(|&b| b == 0x20)
        .unwrap_or(0);

    let (software, comments) = if space_pos == 0 {
        // No comments. Everything up to \CRLF is the software version.
        (String::from_utf8_lossy(&data[cursor..eol_pos]).trim().to_string(), None)
    } else {
        // We have comments following the software version.
        (String::from_utf8_lossy(&data[cursor..cursor+space_pos+1]).trim().to_string(),
         Some(String::from_utf8_lossy(&data[cursor+space_pos..eol_pos]).trim().to_string()))
    };

    Ok((eol_pos+skip_eol, SshVersion {
        version,
        software,
        comments
    }))
}

// Only identifies if the slice contains a KEXI and does not attempt to parse it.
fn identify_kex_init(data: &[u8], kexi_len: usize) -> Result<(), Error> {
    if data.len() < kexi_len || data.len() < 2{
        bail!("Payload too short.");
    }

    let padding = data[0];
    let message_code = data[1];

    if message_code != 0x14 {
        bail!("Invalid key exchange init message code. Expected <20>, got <{}>.", message_code);
    }

    let trailing_count: usize = (padding as usize)+1;

    if data.len() < trailing_count {
        bail!("Payload too short for padding and sequence number.");
    }

    // Skip over entire KEXI length and confirm trailing bytes are padding + sequence number 0.
    if !data[data.len() - trailing_count..].iter().all(|&x| x == 0x00) {
        bail!("Invalid trailing bytes.")
    }

    Ok(())
}