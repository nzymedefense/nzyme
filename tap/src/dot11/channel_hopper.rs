use neli::router::synchronous::NlRouter;
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
    /* Many many more elided */
}
impl neli::consts::genl::Cmd for Nl80211Command {}

#[neli::neli_enum(serialized_type = "u16")]
pub enum Nl80211Attribute {
    Unspecified = 0,

    Wiphy = 1,
    /* Literally hundreds elided */
}
impl neli::consts::genl::NlAttrType for Nl80211Attribute {}

fn handle(msg: Nlmsghdr<GenlId, Genlmsghdr<Nl80211Command, Nl80211Attribute>>) {
    println!("msg={:?}", msg);
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
    
        let recv = sock.send(
            family_id,
            NlmF::DUMP | NlmF::ACK,
            NlPayload::Payload(
                GenlmsghdrBuilder::<Nl80211Command, Nl80211Attribute, NoUserHeader>::default()
                    .cmd(Nl80211Command::GetWiPhy)
                    .version(1)
                    .build().unwrap(),
            ),
        ).unwrap();
    
        for msg in recv {
            let msg = msg.unwrap();
            handle(msg);
        }
    }

}