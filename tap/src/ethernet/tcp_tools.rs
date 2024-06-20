use chrono::{DateTime, Utc};
use crate::ethernet::packets::GenericConnectionStatus;
use crate::ethernet::tables::tcp_table::{TcpSession, TcpSessionState};

pub fn determine_tcp_session_state(session: &TcpSession) 
    -> (GenericConnectionStatus, Option<DateTime<Utc>>) {
    
    match session.state {
        TcpSessionState::SynSent
        | TcpSessionState::SynReceived
        | TcpSessionState::Established
        | TcpSessionState::FinWait1
        | TcpSessionState::FinWait2 => (GenericConnectionStatus::Active, None),
        TcpSessionState::ClosedFin
        | TcpSessionState::ClosedRst
        | TcpSessionState::Refused => (GenericConnectionStatus::Inactive, session.end_time),
        TcpSessionState::ClosedTimeout =>
            (GenericConnectionStatus::InactiveTimeout, session.end_time)
    }
}
