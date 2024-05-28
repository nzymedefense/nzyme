use anyhow::{bail, Error};
use log::info;
use crate::data::tcp_table::TcpSession;
use crate::ethernet::packets::{SocksTunnel, SshSession, SshVersion};

const SSH_GREETING: &[u8] = b"SSH-2.0-";

pub fn tag(cts: &[u8], stc: &[u8], session: &TcpSession) -> Option<SshSession> {
    if cts.len() < 11 || stc.len() < 11 {
        return None
    }

    if cts.starts_with(SSH_GREETING) && stc.starts_with(SSH_GREETING) {
        let client_version = match parse_version_string(cts) {
            Ok(version) => version,
            Err(_) => return None
        };

        let server_version = match parse_version_string(stc) {
            Ok(version) => version,
            Err(_) => return None
        };

        info!("CLIENT VERSION: {:?}", client_version);
        info!("SERVER VERSION: {:?}", server_version);
    }

    None
}

fn parse_version_string(data: &[u8]) -> Result<SshVersion, Error> {
    /*
     * Find EOL. The standard asks for \CRLF but there may also only be a \LF. We will also
     * take the safe route and expect just a \CR as well.
     *
     * To keep it extra easy, we'll also just cut a potential \CR or \LF off the last parsed
     * string at the end. This way we deal with 1 and 2 control characters for EOL.
     */
    let eol_pos = match data.iter().position(|&b| b == 0x0D) {
        Some(pos) => {
            // We have a \CR. Is it followed by an \LF?
            if data.len() < pos+1 {
                bail!("Payload too short.")
            }

            if data[pos+1] != 0x0A {
                // Not followed by \LF. Fine, we'll take the \CR as EOL character.
                pos
            } else {
                // Followed by \CR. We have \CRLF and return the \LF as EOL character.
                pos+1
            }
        },
        None => {
            // No CR at all. Do we have a LF?
            match data.iter().position(|&b| b == 0x0A) {
                Some(pos) => pos,
                None => bail!("No CR or LF control characters. No EOL.")
            }
        }
    };

    // Confirm \CR followed by \LF.


    let mut cursor = 4;
    let dash2_pos = match data[cursor..].iter().position(|&b| b == b'-') {
        Some(pos) => pos,
        None => bail!("No second dash character.")
    };

    // At least one char and \CRLF.
    if data.len() < dash2_pos+3 {
        bail!("Payload too short.")
    }

    let version = String::from_utf8_lossy(&data[cursor..cursor+dash2_pos]).to_string();
    cursor += dash2_pos;

    let space_pos = data[cursor..].iter()
        .position(|&b| b == b' ')
        .unwrap_or(0);

    let (software, comments) = if space_pos == 0 {
        // No comments. Everything up to \CRLF is the software version.
        if data.len() < eol_pos {
            bail!("Payload too short.")
        }

        (String::from_utf8_lossy(&data[space_pos..eol_pos]).to_string(), None)
    } else {
        // We have comments following the software version.
        ("FOO".to_string(), Some("BAR".to_string()))
    };

    // TODO cut potential CR or LF off the end of the string.

    Ok(SshVersion {
        version,
        software,
        comments
    })
}