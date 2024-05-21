use byteorder::{BigEndian, ByteOrder};
use log::info;
use crate::data::tcp_table::{TcpSession, TcpSessionState};
use crate::ethernet::packets::{SocksConnectionHandshakeStatus, SocksConnectionStatus, SocksTunnel, SocksType};
use crate::ethernet::packets::SocksType::{Socks4, Socks4A};
use crate::helpers::network::{string_up_to_null_byte, to_ipv4_address};

pub fn tag(cts: &[u8], stc: &[u8], session: &TcpSession) -> Option<SocksTunnel> {

    if cts.len() < 9 || stc.len() < 2 {
        return None;
    }

    if *cts.first().unwrap() == 0x04
        && (*cts.get(1).unwrap() == 0x01 || *cts.get(1).unwrap() == 0x02) {
        // Potentially SOCKS4, check if the response was SOCKS as well.
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

    if *cts.first().unwrap() == 0x05 && *cts.get(1).unwrap() <= 9 
        && *cts.get(2).unwrap() <= 9 {
        // Potentially SOCKS5, check if the response was SOCKS as well.
        if *stc.first().unwrap() != 0x05 || *stc.get(1).unwrap() > 9 {
            return None;
        }
        
        info!("SOCKS5");

        // TODO update bounds check for cts on top of method

        return None
    }

    None
}

fn is_socks_4a_address(address: &[u8]) -> bool {
    address.len() == 4
        && address[0] == 0x00
        && address[1] == 0x00
        && address[2] == 0x00
        && address[3] != 0x00
}