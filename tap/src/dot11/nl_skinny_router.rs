use std::fmt::Debug;
use std::io;
use std::iter::once;
use std::os::fd::AsRawFd;
use log::trace;
use neli::consts::nl::{GenlId, NlmF, Nlmsg, NlType};
use neli::consts::socket::{NlFamily};
use neli::err::SocketError;
use neli::nl::{NlmsghdrBuilder, NlPayload};
use neli::{FromBytesWithInput, Size, ToBytes};
use neli::consts::genl::{CtrlAttr, CtrlCmd};
use neli::genl::{AttrTypeBuilder, Genlmsghdr, GenlmsghdrBuilder, NlattrBuilder};
use neli::socket::synchronous::NlSocketHandle;
use neli::types::{GenlBuffer, NlBuffer};
use neli::utils::Groups;

/// Low level access to a netlink socket.
/// This is NOT a thread-safe wrapper around a socket. Use accordingly.
pub struct NlSkinnyRouter {
    socket: NlSocketHandle,
    seq: u32
}

impl NlSkinnyRouter {

    pub fn connect(proto: NlFamily) -> Result<Self, SocketError> {
        let socket = NlSocketHandle::connect(proto, Some(0), Groups::empty())?;

        let fd = socket.as_raw_fd();
        let buf_size: libc::c_int = 8192;
        match unsafe { libc::setsockopt(fd, libc::SOL_SOCKET, libc::SO_SNDBUF,  &buf_size as *const _ as *const libc::c_void, std::mem::size_of_val(&buf_size) as u32) } {
            0 => (),
            _ => {
                return Err(SocketError::from(io::Error::last_os_error()));
            }
        }
        match unsafe { libc::setsockopt(fd, libc::SOL_SOCKET, libc::SO_RCVBUF,  &buf_size as *const _ as *const libc::c_void, std::mem::size_of_val(&buf_size) as u32) } {
            0 => (),
            _ => {
                return Err(SocketError::from(io::Error::last_os_error()));
            }
        }

        socket.enable_ext_ack(false)?;

        Ok(NlSkinnyRouter {
            socket,
            seq: 0,
        })

    }

    // implement:
    // send()

    pub fn send<ST, SP>(
        &mut self,
        nl_type: ST,
        nl_flags: NlmF,
        nl_payload: NlPayload<ST, SP>,
    ) -> Result<(), SocketError>
        where
            ST: NlType,
            SP: Size + ToBytes,
    {
        let msg = NlmsghdrBuilder::default()
            .nl_type(nl_type)
            .nl_flags(
                // Required for messages
                nl_flags | NlmF::REQUEST,
            )
            .nl_pid(self.socket.pid())
            .nl_seq(self.next_seq())
            .nl_payload(nl_payload)
            .build()?;

        self.socket.send(&msg)?;

        Ok(())
    }

    // recv() - receives all messages from the socket and returns them in a buffer
    // recv_iter() - receives all messages from the socket and returns them in an iterator
    pub fn recv<T, P>(&self) -> Option<Result<NlBuffer<T, P>, SocketError>>
        where
            T: NlType + Debug,
            P: Size + FromBytesWithInput<Input = usize> + Debug, {
        let mut all_msgs = NlBuffer::new();
        //let mut all_msgs : Vec<Nlmsghdr<T, P>> = Vec::new();

        let mut continue_reading = true;

        while continue_reading {

            match self.socket.recv::<T, P>() {
                Ok((iter, _)) => {
                    for msg in iter {
                        trace!("Message received: {:?}", msg);
                        match msg {
                            Ok(mut m) => {
                                let nl_type = Nlmsg::from((*m.nl_type()).into());
                                //if let NlPayload::Ack(_) = m.nl_payload() {
                                if m.nl_flags().contains(NlmF::ACK) {
                                    // we should never have the kernel sending an ACK request here
                                    //continue_reading = false;
                                    return Some(Err(SocketError::new("Error: ACK received")));
                                } else if let Some(_) = m.get_err() {
                                    //continue_reading = false;
                                    return Some(Err(SocketError::new("Error: Netlink sent a packet error")));
                                } else if nl_type == Nlmsg::Noop {
                                    // just ignore the message
                                    continue;
                                } else if nl_type == Nlmsg::Done {
                                    continue_reading = false;
                                } else if nl_type == Nlmsg::Overrun || nl_type == Nlmsg::Error {
                                    if let NlPayload::Ack(_) = m.nl_payload() {
                                        // this is just a ext_ack, nothing to read, just be done
                                        continue_reading = false;
                                    } else {
                                        return Some(Err(SocketError::new("Error: Netlink sent a packet error")));
                                    }
                                } else {
                                    all_msgs.push(m);
                                }
                            }
                            Err(_) => {
                                continue_reading = false;
                            }
                        }
                    }
                }
                Err(_) => {
                    return Some(Err(SocketError::new("Error: Netlink socket error")));
                }
            }
        }

        Some(Ok(all_msgs))
    }


    /// Convenience function for resolving a [`str`] containing the
    /// generic netlink family name to a numeric generic netlink ID.
    pub fn resolve_genl_family(
        &mut self,
        family_name: &str,
    ) -> Result<u16, SocketError> {
        let mut res = Err(SocketError::new(format!(
            "Generic netlink family {family_name} was not found"
        )));

        let nlhdrs = self.get_genl_family(family_name)?;
        for nlhdr in nlhdrs.into_iter() {
            if let NlPayload::Payload(p) = nlhdr.nl_payload() {
                let handle = p.attrs().get_attr_handle();
                if let Ok(u) = handle.get_attr_payload_as::<u16>(CtrlAttr::FamilyId) {
                    res = Ok(u);
                }
            }
        }

        res
    }

    fn next_seq(&mut self) -> u32 {
        self.seq += 1;
        self.seq
    }

    fn get_genl_family(&mut self, family_name: &str) -> Result<NlBuffer<GenlId, Genlmsghdr<CtrlCmd, CtrlAttr>>, SocketError> {
        self.send(
            GenlId::Ctrl,
            NlmF::ACK,
            NlPayload::Payload(
                GenlmsghdrBuilder::default()
                    .cmd(CtrlCmd::Getfamily)
                    .version(2)
                    .attrs(
                        once(
                            NlattrBuilder::default()
                                .nla_type(
                                    AttrTypeBuilder::default()
                                        .nla_type(CtrlAttr::FamilyName)
                                        .build()?,
                                )
                                .nla_payload(family_name)
                                .build()?,
                        )
                            .collect::<GenlBuffer<_, _>>(),
                    )
                    .build()?,
            ),
        )?;

        let buffer = self.recv();

        match buffer {
            Some(x) => x,
            None => Err(SocketError::new("Error: Unable to resolve generic netlink family")),
        }
    }
}