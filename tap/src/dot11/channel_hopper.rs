use std::collections::HashMap;
use std::iter::once;

use log::info;
use byteorder::{LittleEndian, ByteOrder};

use neli::err::RouterError;
use neli::genl::{NlattrBuilder, AttrTypeBuilder};
use neli::router::synchronous::NlRouter;
use neli::types::GenlBuffer;
use neli::{
    consts::{
        nl::{GenlId, NlmF},
        socket::NlFamily,
    },
    genl::{Genlmsghdr, GenlmsghdrBuilder, NoUserHeader},
    nl::{NlPayload, Nlmsghdr},
    utils::Groups,
};

#[neli::neli_enum(serialized_type = "u8")]
pub enum Nl80211Command {
    Unspecified = 0,
    GetWiPhy = 1,
    SetWiphy = 2,
    GetIf = 5,
    SetChannel = 65
}
impl neli::consts::genl::Cmd for Nl80211Command {}

#[neli::neli_enum(serialized_type = "u16")]
pub enum Nl80211Attribute {
    Unspecified = 0,
    IfIndex = 3,
    IfName = 4,
    IfType = 5,
    WiPhyFreq = 38
}
impl neli::consts::genl::NlAttrType for Nl80211Attribute {}

#[derive(Debug)]
struct InterfaceResponse {
    pub name: String,
    pub dev_index: u8,
    pub dev_type: u32,
}


fn handle_interface_response(msg: Nlmsghdr<GenlId, Genlmsghdr<Nl80211Command, Nl80211Attribute>>) -> InterfaceResponse {
    let mut name: String = "".to_string();
    let mut dev_index: u8 = 0;
    let mut dev_type: u32 = 0;

    for attr in msg.get_payload().unwrap().attrs().iter() {
        if attr.nla_type().nla_type().eq(&Nl80211Attribute::IfIndex) {
            dev_index = attr.nla_payload().as_ref()[0];
        }

        if attr.nla_type().nla_type().eq(&Nl80211Attribute::IfName) {
            let mut bts = attr.nla_payload().as_ref().to_vec();
            bts.pop();
            name = String::from_utf8(bts).unwrap();
        }

        if attr.nla_type().nla_type().eq(&Nl80211Attribute::IfType) {
            dev_type = LittleEndian::read_u32(attr.nla_payload().as_ref());
        }
    }

    InterfaceResponse { name, dev_index, dev_type }
}

pub struct ChannelHopper {

}

impl ChannelHopper {

    pub fn initialize(&self) {
        let (sock, _) = NlRouter::connect(
            NlFamily::Generic, /* family */
            Some(0),           /* pid */
            Groups::empty(),   /* groups */
        ).unwrap();
        let family_id = sock.resolve_genl_family("nl80211").unwrap();
    
        /*let recv_if = sock.send(
            family_id,
            NlmF::DUMP | NlmF::ACK,
            NlPayload::Payload(
                GenlmsghdrBuilder::<Nl80211Command, Nl80211Attribute, NoUserHeader>::default()
                    .cmd(Nl80211Command::GetIf)
                    .version(1)
                    .build()
                    .unwrap()
            ),
        ).unwrap();
    
        let mut interfaces: HashMap<String, InterfaceResponse> = HashMap::new();
        for msg in recv_if {
            let msg = msg.unwrap();
            let response = handle_interface_response(msg);
            interfaces.insert(response.name.clone(), response);
        }


        println!("ifs: {:?}", interfaces);*/




        let attr_idx = once(
            NlattrBuilder::default()
                .nla_type(
                    AttrTypeBuilder::default()
                        .nla_type(Nl80211Attribute::IfIndex)
                        .build()
                        .unwrap(),
                )
                .nla_payload(5)
                .build()
                .unwrap(),
        );

        let attr_freq = once(
            NlattrBuilder::default()
                .nla_type(
                    AttrTypeBuilder::default()
                        .nla_type(Nl80211Attribute::WiPhyFreq)
                        .build()
                        .unwrap(),
                )
                .nla_payload(2412)
                .build()
                .unwrap(),
        );

        let payload = NlPayload::Payload(
            GenlmsghdrBuilder::<Nl80211Command, Nl80211Attribute, NoUserHeader>::default()
                .cmd(Nl80211Command::SetChannel)
                .version(1)
                .attrs(attr_idx.chain(attr_freq).collect())
                .build()
                .unwrap()
        );

        let recv_channel_resp: neli::router::synchronous::NlRouterReceiverHandle<GenlId, Genlmsghdr<Nl80211Command, Nl80211Attribute>> = sock.send(family_id, NlmF::empty(), payload).unwrap();

        for msg in recv_channel_resp {
            match msg {
                Ok(msg) => info!("success: {:?}", msg),
                Err(RouterError::Nlmsgerr(e)) => {
                    // Should be packet that caused error
                    println!("Router Error: {:?}", e);
                }
                Err(e) => panic!("Error: {:?}", e),
            };
        }
    }

}