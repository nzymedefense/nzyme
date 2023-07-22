use std::collections::HashMap;
use std::iter::once;

use anyhow::{Error, bail};
use byteorder::{LittleEndian, ByteOrder};

use neli::genl::{NlattrBuilder, AttrTypeBuilder};
use neli::router::synchronous::{NlRouter, NlRouterReceiverHandle};
use neli::{
    consts::{
        nl::{GenlId, NlmF},
        socket::NlFamily,
    },
    genl::{Genlmsghdr, GenlmsghdrBuilder, NoUserHeader},
    nl::{NlPayload, Nlmsghdr},
    utils::Groups,
};
use neli::consts::rtnl::{Arphrd, RtAddrFamily, Rtm};
use neli::rtnl::{Ifinfomsg, IfinfomsgBuilder};
use neli::types::Buffer;

#[neli::neli_enum(serialized_type = "u8")]
pub enum Nl80211Command {
    Unspecified = 0,
    GetWiPhy = 1,
    GetIf = 5,
    SetIf = 6,
    SetChannel = 65
}
impl neli::consts::genl::Cmd for Nl80211Command {}

#[neli::neli_enum(serialized_type = "u16")]
pub enum Nl80211Attribute {
    Unspecified = 0,
    WiPhy = 1,
    IfIndex = 3,
    IfName = 4,
    IfType = 5,
    WiPhyBands = 22,
    MntrFlags = 23,
    WiPhyFreq = 38,
    SplitWiphyDump = 174
}
impl neli::consts::genl::NlAttrType for Nl80211Attribute {}

#[neli::neli_enum(serialized_type = "u16")]
pub enum Nl80211MntrFlags {
    Control = 3,
    OtherBss = 4
}
impl neli::consts::genl::NlAttrType for Nl80211MntrFlags {}

#[derive(Debug)]
struct InterfaceResponse {
    pub name: String,
    pub phy_index: u32,
    pub if_index: u32,
}

#[derive(Debug)]
struct PhyResponse {
    phy_index: u32,
    supported_frequencies: Vec<u32>
}

#[derive(Debug, Clone)]
pub struct DeviceSummary {
    pub name: String,
    pub if_index: u32,
    pub supported_frequencies: Vec<u32>
}

pub enum InterfaceState {
    Up, Down
}

fn handle_interface_response(msg: Nlmsghdr<GenlId, Genlmsghdr<Nl80211Command, Nl80211Attribute>>) -> InterfaceResponse {
    let mut name: String = "".to_string();
    let mut phy_index: u32 = 0;
    let mut if_index: u32 = 0;

    for attr in msg.get_payload().unwrap().attrs().iter() {
        if attr.nla_type().nla_type().eq(&Nl80211Attribute::WiPhy) {
            phy_index = LittleEndian::read_u32(&attr.nla_payload().as_ref()[0..4]);
        }

        if attr.nla_type().nla_type().eq(&Nl80211Attribute::IfIndex) {
            if_index = LittleEndian::read_u32(&attr.nla_payload().as_ref()[0..4]);
        }

        if attr.nla_type().nla_type().eq(&Nl80211Attribute::IfName) {
            let mut bts = attr.nla_payload().as_ref().to_vec();
            bts.pop();
            name = String::from_utf8(bts).unwrap();
        }
    }

    InterfaceResponse { name, phy_index, if_index }
}

fn handle_phy_response(msg: Nlmsghdr<GenlId, Genlmsghdr<Nl80211Command, Nl80211Attribute>>) -> PhyResponse {
    let mut phy_index: u32 = 0;
    let mut supported_frequencies: Vec<u32> = Vec::new();

    for attr in msg.get_payload().unwrap().attrs().iter() {
        if attr.nla_type().nla_type().eq(&Nl80211Attribute::WiPhy) {
            phy_index = LittleEndian::read_u32(&attr.nla_payload().as_ref()[0..4]);
        }

        if attr.nla_type().nla_type().eq(&Nl80211Attribute::WiPhyBands) {
            let payload = attr.nla_payload().as_ref();

            let mut cursor: usize = 0;
            loop {
                if cursor+2 > payload.len() {
                    break;
                }

                let element_length: usize = LittleEndian::read_u16(&payload[cursor..cursor+2]) as usize;
                cursor += 2;

                // Skip index attribute.
                cursor += 2;

                if payload.len() < element_length {
                    // Some PHYs report this attribute but are empty.
                    continue;
                }

                let mut element_complete = false;
                loop {
                    let start = cursor;
                    
                    let length = LittleEndian::read_u16(&payload[cursor..cursor+2]);
                    cursor += 2;
                    let attribute_type = LittleEndian::read_u16(&payload[cursor..cursor+2]);
                    cursor += 2;

                    if attribute_type == 1 {
                        let freqs = &payload[cursor..cursor-4+length as usize];

                        let mut freq_cursor = 0;
                        loop {
                            let start = freq_cursor;
                            let freq_length = LittleEndian::read_u16(&freqs[freq_cursor..freq_cursor+2]);
                            freq_cursor = start+freq_length as usize;

                            if let Some(f) = parse_frequency_from_attributes(&freqs[start..start+freq_length as usize]) {
                                supported_frequencies.push(f);
                            }

                            if length == (freq_cursor+4) as u16 {
                                break;
                            }
                        }
                    }

                    cursor = start+length as usize;

                    /*
                    * Cycle through padding or until you reach end of this attribute.
                    * Each PHY band (2.4Ghz, 5Ghz, etc.) is in own attribute group.
                    */ 
                    loop {
                        if cursor == element_length || cursor == payload.len() {
                            element_complete = true;
                            break;
                        }

                        if payload[cursor] != 0x00 {
                            break;
                        } else {
                            cursor += 1
                        }
                    }

                    if element_complete {
                        break;
                    }
                }
            }
        }
    }

    PhyResponse { phy_index, supported_frequencies }
}

fn parse_frequency_from_attributes(attr: &[u8]) -> Option<u32> {
    let mut cursor = 0;
    let length = LittleEndian::read_u16(&attr[cursor..cursor+2]);
    cursor += 2;

    // Skip index attribute.
    cursor += 2;

    let mut frequency = 0;
    loop {
        let start = cursor;
        let attr_length = LittleEndian::read_u16(&attr[cursor..cursor+2]);
        cursor += 2;
    
        let attr_type = LittleEndian::read_u16(&attr[cursor..cursor+2]);
        cursor += 2;

        if attr_type == 1 {
            frequency = LittleEndian::read_u32(&attr[cursor..cursor+4]);
        }

        if attr_type == 2 {
            // Frequency has the `disabled` attribute due to current regulatory domain.
            return None
        }

        cursor = start+attr_length as usize;

        if cursor == length as usize {
            break;
        }
    }

    if frequency != 0 {
        Some(frequency)
    } else {
        None
    }
}

pub struct Nl {
    dot11_socket: NlRouter,
    dot11_family_id: u16,
    rt_socket: NlRouter,
    devices: HashMap<String, DeviceSummary>
}

impl Nl {

    pub fn new() -> Result<Self, Error> {
        let (dot11_socket, _) = match NlRouter::connect(NlFamily::Generic, Some(0), Groups::empty()) {
            Ok(sock) => sock,
            Err(e) => bail!("Could not open Netlink 802.11 socket: {}", e)
        };

        let dot11_family_id = match dot11_socket.resolve_genl_family("nl80211") {
            Ok(family_id) => family_id,
            Err(e) => bail!("Could not resolve Netlink family: {}", e)
        };

        let (rt_socket, _) = match NlRouter::connect(NlFamily::Route, None, Groups::empty()) {
            Ok(sock) => sock,
            Err(e) => bail!("Could not open Netlink route socket: {}", e)
        };

        let devices = HashMap::new();

        Ok(Self {
            dot11_socket,
            dot11_family_id,
            devices,
            rt_socket
        })
    }

    pub fn fetch_device(&mut self, device_name: &String) -> Result<DeviceSummary, Error> {
        if let Some(device) = self.devices.get(device_name) {
            return Ok(device.clone());
        }

        let get_if_payload = match GenlmsghdrBuilder::<Nl80211Command, Nl80211Attribute, NoUserHeader>::default()
                    .cmd(Nl80211Command::GetIf)
                    .version(1)
                    .build() {
                Ok(pl) => pl,
                Err(e) => bail!("Could not build GetIf Netlink payload: {}", e)
        };

        let recv_if = match self.dot11_socket.send(self.dot11_family_id, NlmF::DUMP, NlPayload::Payload(get_if_payload)) {
            Ok(recv) => recv,
            Err(e) => bail!("Could not send GetIf Netlink command: {}", e)
        };

        let mut interfaces: HashMap<String, InterfaceResponse> = HashMap::new();
        for msg in recv_if {
            match msg {
                Ok(msg) => {
                    let interface = handle_interface_response(msg);
                    interfaces.insert(interface.name.clone(), interface);
                },
                Err(e) => bail!("Could not parse GetIf Netlink response: {}", e)
            };
        }

        let interface_info = match interfaces.get(device_name) {
            Some(interface) => interface,
            None => bail!("Interface [{}] not found.", device_name)
        };

        let attr_filter_wiphy_dump_type = match AttrTypeBuilder::default().nla_type(Nl80211Attribute::WiPhy).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct WiPhy Netlink attribute type: {}", e)
        };

        let attr_filter_wiphy_dump = match NlattrBuilder::default().nla_type(attr_filter_wiphy_dump_type)
                                        .nla_payload(interface_info.phy_index).build() {
            Ok(attr) => attr,
            Err(e) => bail!("Could not construct WiPhy Netlink attribute: {}", e)
        };

        let get_wiphy = match GenlmsghdrBuilder::<Nl80211Command, Nl80211Attribute, NoUserHeader>::default()
                    .cmd(Nl80211Command::GetWiPhy)
                    .version(1)
                    .attrs(once(attr_filter_wiphy_dump).collect())
                    .build() {
                Ok(pl) => pl, 
                Err(e) => bail!("Could not build GetWiPhy Netlink payload: {}", e)
        };

        let recv_phy = match self.dot11_socket.send(self.dot11_family_id, NlmF::DUMP, NlPayload::Payload(get_wiphy)) {
            Ok(recv) => recv,
            Err(e) => bail!("Could not send GetWiPhy Netlink command: {}", e)
        };

        let mut phys: HashMap<u32, PhyResponse> = HashMap::new(); 
        for msg in recv_phy {
            match msg {
                Ok(msg) => {
                    let phy = handle_phy_response(msg);
                    phys.insert(phy.phy_index, phy);
                },
                Err(e) => bail!("Could not parse GetWiPhy Netlink response: {}", e)
            };            
        }

        let phy_info = match phys.get(&interface_info.phy_index) {
            Some(phy) => phy,
            None => bail!("Phy #[{}] not found.", interface_info.phy_index)
        };

        let device_summary = DeviceSummary {
            name: interface_info.name.clone(),
            supported_frequencies: phy_info.supported_frequencies.clone(),
            if_index: interface_info.if_index
        };

        self.devices.insert(device_name.clone(), device_summary.clone());

        Ok(device_summary)
    }

    pub fn set_device_frequency(&mut self, device_name: &String, frequency: u32) -> Result<(), Error> {
        let device = match self.fetch_device(device_name) {
            Ok(device) => device,
            Err(e) => bail!("Could not load device with name [{}] for setting frequency: {}", device_name, e)
        };

        let attr_if_index_type = match AttrTypeBuilder::default().nla_type(Nl80211Attribute::IfIndex).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct IfIndex Netlink attribute type: {}", e)
        };

        let attr_if_index = match NlattrBuilder::default().nla_type(attr_if_index_type)
                                    .nla_payload(device.if_index).build() {
            Ok(attr) => attr,
            Err(e) => bail!("Could not construct IfIndex Netlink attribute: {}", e) 
        };

        let attr_wiphy_freq_type = match AttrTypeBuilder::default().nla_type(Nl80211Attribute::WiPhyFreq).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct WiPhyFreq Netlink attribute type: {}", e)
        };

        let attr_iwiphy_freq_index = match NlattrBuilder::default().nla_type(attr_wiphy_freq_type)
                                        .nla_payload(frequency).build() {
            Ok(attr) => attr,
            Err(e) => bail!("Could not construct WiPhyFreq Netlink attribute: {}", e)
        };

        let payload = match GenlmsghdrBuilder::<Nl80211Command, Nl80211Attribute, NoUserHeader>::default()
                            .cmd(Nl80211Command::SetChannel)
                            .version(1)
                            .attrs(vec![attr_if_index, attr_iwiphy_freq_index].into_iter().collect())
                            .build() {
            Ok(p) => p,
            Err(e) => bail!("Could not construct WiPhyFreq Netlink command payload: {}", e)
        };

        let _: NlRouterReceiverHandle<GenlId, Genlmsghdr<Nl80211Command, Nl80211Attribute>> = 
                                    match self.dot11_socket.send(self.dot11_family_id, NlmF::empty(), NlPayload::Payload(payload)){
            Ok(recv) => recv,
            Err(e) => bail!("Could not send WiPhyFreq Netlink command: {}", e)
        };

        Ok(())
    }

    pub fn enable_monitor_mode(&mut self, device_name: &String) -> Result<(), Error> {
        let device = match self.fetch_device(device_name) {
            Ok(device) => device,
            Err(e) => bail!("Could not load device with name [{}] for setting frequency: {}", device_name, e)
        };

        let attr_if_index_type = match AttrTypeBuilder::default().nla_type(Nl80211Attribute::IfIndex).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct IfIndex Netlink attribute type: {}", e)
        };

        let attr_if_index = match NlattrBuilder::default().nla_type(attr_if_index_type)
            .nla_payload(device.if_index).build() {
            Ok(attr) => attr,
            Err(e) => bail!("Could not construct IfIndex Netlink attribute: {}", e)
        };

        let attr_if_type_type = match AttrTypeBuilder::default().nla_type(Nl80211Attribute::IfType).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct IfType Netlink attribute type: {}", e)
        };

        let attr_if_type = match NlattrBuilder::default().nla_type(attr_if_type_type)
            .nla_payload(6).build() { // Monitor type.
            Ok(attr) => attr,
            Err(e) => bail!("Could not construct IfType Netlink attribute: {}", e)
        };

        let attr_monitor_flag_control_type = match AttrTypeBuilder::default().nla_type(Nl80211MntrFlags::Control).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct Nl80211MntrFlags::Control Netlink attribute type: {}", e)
        };

        let attr_monitor_flag_control = match NlattrBuilder::default().nla_type(attr_monitor_flag_control_type)
            .nla_payload(Buffer::new()).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct Nl80211MntrFlags::Control Netlink attribute: {}", e)
        };

        let attr_monitor_flag_otherbss_type = match AttrTypeBuilder::default().nla_type(Nl80211MntrFlags::OtherBss).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct Nl80211MntrFlags::OtherBss Netlink attribute type: {}", e)
        };

        let attr_monitor_flag_otherbss = match NlattrBuilder::default().nla_type(attr_monitor_flag_otherbss_type)
            .nla_payload(Buffer::new()).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct Nl80211MntrFlags::OtherBss Netlink attribute: {}", e)
        };

        let attr_monitor_flags_type = match AttrTypeBuilder::default().nla_type(Nl80211Attribute::MntrFlags).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct MntrFlags Netlink attribute type: {}", e)
        };

        let attr_monitor_flags = NlattrBuilder::default().nla_type(attr_monitor_flags_type)
            .nla_payload(vec![attr_monitor_flag_otherbss, attr_monitor_flag_control])
            .build()?;

        let payload = match GenlmsghdrBuilder::<Nl80211Command, Nl80211Attribute, NoUserHeader>::default()
            .cmd(Nl80211Command::SetIf)
            .version(0)
            .attrs(vec![attr_if_index, attr_if_type, attr_monitor_flags].into_iter().collect())
            .build() {
            Ok(p) => p,
            Err(e) => bail!("Could not construct monitor mode Netlink command payload: {}", e)
        };

        let _: NlRouterReceiverHandle<GenlId, Genlmsghdr<Nl80211Command, Nl80211Attribute>> =
            match self.dot11_socket.send(self.dot11_family_id, NlmF::REQUEST | NlmF::ACK, NlPayload::Payload(payload)){
                Ok(recv) => recv,
                Err(e) => bail!("Could not send WiPhyFreq Netlink command: {}", e)
            };

        Ok(())
    }

    pub fn change_80211_interface_state(&mut self, device_name: &String, state: InterfaceState)
            -> Result<(), Error>  {
        let device = match self.fetch_device(device_name) {
            Ok(device) => device,
            Err(e) => bail!("Could not load device with name [{}] for changing state: {}",
                device_name, e)
        };

        /*
         * Setting interface family to 802.11 with radiotap header (value 803) in this call. This
         * would have to be different if we ever wanted to control standard ethernet links.
         */
        let msg_builder = IfinfomsgBuilder::default()
            .ifi_index(device.if_index as i32)
            .ifi_family(RtAddrFamily::Inet)
            .ifi_type(Arphrd::UnrecognizedConst(803));

        let msg = match state {
            InterfaceState::Up => msg_builder.up().build()?,
            InterfaceState::Down => msg_builder.down().build()?
        };

        let _: NlRouterReceiverHandle<GenlId, Nlmsghdr<Rtm, Ifinfomsg>> =
            match self.rt_socket.send(
                Rtm::Setlink,
                NlmF::ROOT | NlmF::ECHO,
                NlPayload::Payload(msg)) {
                Ok(recv) => recv,
                Err(e) => { bail!("Could not send request: {}", e); }
            };

        Ok(())
    }


}