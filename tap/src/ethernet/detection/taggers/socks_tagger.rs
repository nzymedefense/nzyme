use anyhow::{bail, Error};
use byteorder::{BigEndian, ByteOrder};
use chrono::Utc;
use log::{debug, info, warn};
use crate::data::tcp_table::{TcpSession, TcpSessionState};
use crate::ethernet::packets::{SocksAuthenticationMethod, SocksAuthenticationResult, SocksConnectionHandshakeStatus, SocksConnectionStatus, SocksTunnel, SocksType};
use crate::ethernet::packets::SocksType::{Socks4, Socks4A};
use crate::helpers::network::{string_up_to_null_byte, to_ipv4_address, to_ipv6_address};

pub fn tag(cts: &[u8], stc: &[u8], session: &TcpSession) -> Option<SocksTunnel> {

    if cts.len() < 9 || stc.len() < 2 {
        return None;
    }

    // SOCKS 4(a)
    if *cts.first().unwrap() == 0x04
        && (*cts.get(1).unwrap() == 0x01 || *cts.get(1).unwrap() == 0x02) {
        // Potentially SOCKS 4(a), check if the response was SOCKS as well.
        if *stc.first().unwrap() != 0x04
            || *stc.get(1).unwrap() < 0x5A
            || *stc.get(1).unwrap() > 0x5D {
            return None
        }

        let tunneled_destination_port = BigEndian::read_u16(&cts[2..4]);

        let tunneled_destination_address = if !is_socks_4a_address(&cts[4..8]) {
            Some(to_ipv4_address(&cts[4..8]))
        } else {
            None
        };

        let username = string_up_to_null_byte(&cts[8..]);

        let cursor = 8 + match &username {
            Some(username) => username.len() + 1,
            None => 1
        };

        let tunneled_destination_host = if tunneled_destination_address.is_none() {
           string_up_to_null_byte(&cts[cursor..])
        } else {
            None
        };

        let handshake_status = match *stc.get(1).unwrap() {
            0x5A => SocksConnectionHandshakeStatus::Granted,
            0x5B => SocksConnectionHandshakeStatus::Rejected,
            0x5C => SocksConnectionHandshakeStatus::FailedIdentdUnreachable,
            0x5D => SocksConnectionHandshakeStatus::FailedIdentdAuth,
            _    => SocksConnectionHandshakeStatus::Invalid,
        };

        if handshake_status == SocksConnectionHandshakeStatus::Invalid {
            return None;
        }

        // Overwrite connection status in case of closed TCP connection.
        let (connection_status, terminated_at) = match session.state {
            TcpSessionState::SynSent
            | TcpSessionState::SynReceived
            | TcpSessionState::Established
            | TcpSessionState::FinWait1
            | TcpSessionState::FinWait2 => (SocksConnectionStatus::Active, None),
            TcpSessionState::ClosedFin
            | TcpSessionState::ClosedRst
            | TcpSessionState::Refused => (SocksConnectionStatus::Inactive, session.end_time),
            TcpSessionState::ClosedTimeout =>
                (SocksConnectionStatus::InactiveTimeout, session.end_time)
        };

        let socks_type = if tunneled_destination_address.is_some() {
            Socks4
        } else {
            Socks4A
        };

        return Some(SocksTunnel {
            socks_type,
            authentication_status: SocksAuthenticationResult::Success,
            handshake_status,
            connection_status,
            username,
            tunneled_bytes: session.bytes_count,
            tcp_session_key: session.session_key.clone(),
            tunneled_destination_address,
            tunneled_destination_host,
            tunneled_destination_port,
            source_mac: session.source_mac.clone(),
            destination_mac: session.destination_mac.clone(),
            source_address: session.source_address,
            destination_address: session.destination_address,
            source_port: session.source_port,
            destination_port: session.destination_port,
            established_at: session.start_time,
            terminated_at
        })
    }

    // SOCKS 5.
    if *cts.first().unwrap() == 0x05 && *cts.get(1).unwrap() <= 9 
        && *cts.get(2).unwrap() <= 9 {
        // Potentially SOCKS5, check if the response was SOCKS as well.
        if *stc.first().unwrap() != 0x05 || *stc.get(1).unwrap() > 9 {
            return None;
        }

        // Parse auth methods supported by client.
        let client_auth_methods_count: usize = *cts.get(1).unwrap() as usize;
        let mut client_auth_methods: Vec<SocksAuthenticationMethod> = vec![];

        let mut cts_cursor: usize = 2;
        let mut stc_cursor: usize = 2;
        if cts.len() < client_auth_methods_count+cts_cursor {
            return None;
        }

        for cam in &cts[cts_cursor..cts_cursor+client_auth_methods_count] {
            client_auth_methods.push(resolve_authentication_method(cam));
        }
        cts_cursor += client_auth_methods_count;

        // Find auth method server chose.
        let server_chosen_auth_method = resolve_authentication_method(stc.get(1).unwrap());

        // Authentication.
        let (auth_result, auth_cts_offset, auth_stc_offset) = match socks5_authentication(
            server_chosen_auth_method,
            cts,
            stc,
            &cts_cursor,
            &stc_cursor)
        {
            Ok(result) => result,
            Err(e) => {
                /*
                 * We couldn't parse the authentication steps, or we don't support the
                 * chosen authentication method.
                 */
                debug!("Could not parse SOCKS5 authentication steps: {}", e);
                return Some(build_auth_failed_result(session, SocksAuthenticationResult::Unknown))
            }
        };

        if auth_result != SocksAuthenticationResult::Success {
            return Some(build_auth_failed_result(session, SocksAuthenticationResult::Failure))
        }

        // We have a completed, successful authentication at this point.
        cts_cursor += auth_cts_offset;
        stc_cursor += auth_stc_offset;

        if cts[cts_cursor] != 0x05 || cts.len() < cts_cursor+4 || stc.len() < stc_cursor+4 {
            return None;
        }

        // Skip command code and a reserved field.
        cts_cursor += 2;

        // Reserved field. Must be 0x00.
        if cts[cts_cursor] != 0x00 {
            return None;
        }

        cts_cursor += 1;

        let socks_address_type = &cts[cts_cursor];
        cts_cursor += 1;

        // Parse tunnel destination address according to type.
        let (tunneled_destination_address, tunnel_destination_host, address_cts_offset) = match socks_address_type {
            0x01 => {
                // IPv4
                if cts.len() < cts_cursor+4 {
                    return None;
                }

                (Some(to_ipv4_address(&cts[cts_cursor..cts_cursor+4])), None, 4usize)
            },
            0x03 => {
                // Domain name
                if cts.len() < cts_cursor+2 {
                    return None;
                }

                let len = cts[cts_cursor];

                if len == 0 {
                    return None;
                }

                let domain = String::from_utf8_lossy(&cts[cts_cursor+1..]).to_string();
                (None, Some(domain), (len+1) as usize)
            },
            0x04 => {
                // IPv6
                if cts.len() < cts_cursor+16 {
                    return None;
                }

                (Some(to_ipv6_address(&cts[cts_cursor..cts_cursor+16])), None, 16usize)
            },
            _ => return None
        };

        // TODO test with Ipv4, Ipv6, Hostname
        info!("ADDR: {:?}, HOST: {:?}", tunneled_destination_address, tunnel_destination_host);

        cts_cursor += address_cts_offset;

        // Parse tunnel port.

        return None
    }

    // Not SOCKS.
    None
}

fn resolve_authentication_method(method: &u8) -> SocksAuthenticationMethod {
    match method {
        0x00 => SocksAuthenticationMethod::None,
        0x01 => SocksAuthenticationMethod::Gssapi,
        0x02 => SocksAuthenticationMethod::UsernamePassword,
        0x03 => SocksAuthenticationMethod::ChallengeHandshake,
        0x05 => SocksAuthenticationMethod::ChallengeResponse,
        0x06 => SocksAuthenticationMethod::Ssl,
        0x07 => SocksAuthenticationMethod::Nds,
        0x08 => SocksAuthenticationMethod::MultiAuthenticationFramework,
        0x09 => SocksAuthenticationMethod::JsonParameterBlock,
        _ => SocksAuthenticationMethod::Unknown
    }
}

// Returns (client, server) bytes to skip.
fn socks5_authentication(method: SocksAuthenticationMethod,
                         cts: &[u8],
                         stc: &[u8],
                         cts_cursor: &usize,
                         stc_cursor: &usize)
    -> Result<(SocksAuthenticationResult, usize, usize), Error> {

    match method {
        SocksAuthenticationMethod::None => Ok((SocksAuthenticationResult::Success, 0, 0)),
        _ => bail!("Authentication method [{:?}] not supported.", method)
    }
}

fn is_socks_4a_address(address: &[u8]) -> bool {
    address.len() == 4
        && address[0] == 0x00
        && address[1] == 0x00
        && address[2] == 0x00
        && address[3] != 0x00
}

fn build_auth_failed_result(session: &TcpSession, auth_result: SocksAuthenticationResult)
    -> SocksTunnel {

    SocksTunnel {
        socks_type: SocksType::Socks5,
        authentication_status: auth_result,
        handshake_status: SocksConnectionHandshakeStatus::NotReached,
        connection_status: SocksConnectionStatus::Inactive,
        username: None,
        tunneled_bytes: 0,
        tunneled_destination_address: None,
        tunneled_destination_host: None,
        tunneled_destination_port: 0,
        tcp_session_key: session.session_key.clone(),
        source_mac: session.source_mac.clone(),
        destination_mac: session.destination_mac.clone(),
        source_address: session.source_address,
        destination_address: session.destination_address,
        source_port: session.source_port,
        destination_port: session.destination_port,
        established_at: session.start_time,
        terminated_at: Some(Utc::now())
    }
}