use std::collections::HashMap;
use std::iter::once;

use anyhow::{Error, bail};

use neli::{
    consts::{
        nl::{NlmF},
        socket::NlFamily,
        rtnl::{Arphrd, RtAddrFamily, Rtm},
    },
    genl::{Genlmsghdr, GenlmsghdrBuilder, NoUserHeader, NlattrBuilder, AttrTypeBuilder},
    nl::{NlPayload, Nlmsghdr},
    utils::Groups,
    router::synchronous::{NlRouter, NlRouterReceiverHandle},
    rtnl::{Ifinfomsg, IfinfomsgBuilder},
    types::Buffer,
};
use neli::attr::Attribute;
use neli::consts::nl::{GenlId};
use neli::err::DeError;
use neli::genl::Nlattr;

use super::nl_enums::{Nl80211Attribute, Nl80211Command, Nl80211BandAttr, Nl80211FrequencyAttr, Nl80211MntrFlags};

#[derive(Debug)]
pub struct Interface {
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
pub struct InterfaceInfo {
    pub name: String,
    pub if_index: u32,
    pub supported_frequencies: Vec<u32>
}

pub enum InterfaceState {
    Up, Down
}

// Parse specific attributes from an 802.11 (wireless) related Netlink message
// and return a structured response with information about a network interface,
// such as its name, physical layer index, and interface index
fn handle_interface_response(msg: Nlmsghdr<GenlId, Genlmsghdr<Nl80211Command, Nl80211Attribute>>) -> Result<Interface, DeError> {
    let mut name: Option<String> = None;
    let mut phy_index: Option<u32> = None;
    let mut if_index: Option<u32> = None;

    let payload = msg.get_payload().ok_or(DeError::new("Failed to get payload"))?;

    for attr in payload.attrs().iter() {
        match attr.nla_type().nla_type() {
            &Nl80211Attribute::WiPhy => {
                phy_index = Some(attr.get_payload_as()?);
            },
            &Nl80211Attribute::IfIndex => {
                if_index = Some(attr.get_payload_as()?);
            },
            &Nl80211Attribute::IfName => {
                name = Some(attr.get_payload_as_with_len::<String>()?);
            },
            _ => {}
        }
    }

    // If any of the required attributes is missing, return an error.
    if name.is_none() || phy_index.is_none() || if_index.is_none() {
        return Err(DeError::new("Missing necessary attributes"));
    }

    Ok(Interface {
        name: name.unwrap(),
        phy_index: phy_index.unwrap(),
        if_index: if_index.unwrap(),
    })
}

fn handle_phy_response(msg: Nlmsghdr<GenlId, Genlmsghdr<Nl80211Command, Nl80211Attribute>>) -> Result<PhyResponse, DeError> {
    let phy_index: u32;
    let mut supported_frequencies: Vec<u32> = Vec::new();

    let payload = msg.get_payload().ok_or(DeError::new("Failed to get payload"))?;
    let payload_handle = payload.attrs().get_attr_handle();

    // Get the WiPhy attribute from the payload, which is a u32
    let wiphy_handle = payload_handle.get_attribute(Nl80211Attribute::WiPhy).ok_or(DeError::new("Failed to get WiPhy attribute"))?;
    phy_index = wiphy_handle.get_payload_as().map_err(|_| DeError::new("Failed to get payload as u32"))?;

    // Get the WiPhyBands attribute from the payload, which is a Vec of attributes
    let bands = payload_handle.get_attr_payload_as_with_len::<Vec<Nlattr<Nl80211BandAttr, Buffer>>>(Nl80211Attribute::WiPhyBands).map_err(|_| DeError::new("Failed to get WiPhyBands attribute"))?;

    for band in bands.iter() {

        // get a handle for the individual attribute, the payload for the individual bands attribute has many attributes nested (NOT a Vec)
        let band_handle = band.get_attr_handle::<Nl80211BandAttr>().map_err(|_| DeError::new("Failed to get WiPhyBands attribute handle"))?;
        // get the Freqs attribute from the nested attributes, which is a Vec of attributes
        let freqs = band_handle.get_attr_payload_as_with_len::<Vec<Nlattr<Nl80211FrequencyAttr, Buffer>>>(Nl80211BandAttr::Freqs).map_err(|_| DeError::new("Failed to get WiPhyFreq attribute"))?;

        for freq in freqs.iter() {
            // get a handle for the individual attribute, the payload for the individual freqs attribute has many attributes nested (NOT a Vec)
            let freq_handle = freq.get_attr_handle::<Nl80211FrequencyAttr>().map_err(|_| DeError::new("Failed to get WiPhyFreq attribute handle"))?;
            let freq_attr = freq_handle.get_attribute(Nl80211FrequencyAttr::Freq);
            let disabled_attr = freq_handle.get_attribute(Nl80211FrequencyAttr::Disabled);

            if freq_attr.is_some() && disabled_attr.is_none() {
                supported_frequencies.push(freq_attr.unwrap().get_payload_as().map_err(|_| DeError::new("Failed to get frequency attribute payload as u32"))?);
            }
        }
    }

    Ok(PhyResponse {
        phy_index,
        supported_frequencies
    })
}

pub struct Nl {
    dot11_socket: NlRouter,
    dot11_family_id: u16,
    rt_socket: NlRouter,
    devices: HashMap<String, InterfaceInfo>
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

    /// Fetch all available 80211 devices, with interface wiphy id and name
    pub fn fetch_devices(&self) -> Result<HashMap<String, Interface>, Error> {
        let get_if_payload = match GenlmsghdrBuilder::<Nl80211Command, Nl80211Attribute, NoUserHeader>::default()
            .cmd(Nl80211Command::GetIf)
            .version(1)
            .build() {
            Ok(pl) => pl,
            Err(e) => bail!("Could not build GetIf Netlink payload: {}", e)
        };

        let mut recv_if = match self.dot11_socket.send(self.dot11_family_id, NlmF::DUMP, NlPayload::Payload(get_if_payload)) {
            Ok(recv) => recv,
            Err(e) => bail!("Could not send GetIf Netlink command: {}", e)
        };

        let mut interfaces: HashMap<String, Interface> = HashMap::new();

        while let Some(Ok(msg)) = recv_if.next() {
            let interface = handle_interface_response(msg);
            match interface {
                Ok(interface) => { interfaces.insert(interface.name.clone(), interface); },
                Err(e) => bail!("Could not parse GetIf Netlink response: {}", e)
            };
        }

        Ok(interfaces)
    }

    pub fn fetch_device_info(&mut self, device_name: &String) -> Result<InterfaceInfo, Error> {
        if let Some(device) = self.devices.get(device_name) {
            return Ok(device.clone());
        }

        let interfaces = match self.fetch_devices() {
            Ok(interfaces) => interfaces,
            Err(e) => bail!("Could not fetch devices: {}", e)
        };

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

        let mut recv_phy = match self.dot11_socket.send(self.dot11_family_id, NlmF::DUMP, NlPayload::Payload(get_wiphy)) {
            Ok(recv) => recv,
            Err(e) => bail!("Could not send GetWiPhy Netlink command: {}", e)
        };

        let mut phys: HashMap<u32, PhyResponse> = HashMap::new();
        while let Some(Ok(msg)) = recv_phy.next() {
            let phy = handle_phy_response(msg);
            match phy {
                Ok(phy) => { phys.insert(phy.phy_index, phy); },
                Err(e) => bail!("Could not parse GetWiPhy Netlink response: {}", e)
            };

        }

        let phy_info = match phys.get(&interface_info.phy_index) {
            Some(phy) => phy,
            None => bail!("Phy #[{}] not found.", interface_info.phy_index)
        };

        let device_summary = InterfaceInfo {
            name: interface_info.name.clone(),
            supported_frequencies: phy_info.supported_frequencies.clone(),
            if_index: interface_info.if_index
        };

        self.devices.insert(device_name.clone(), device_summary.clone());

        Ok(device_summary)
    }

    pub fn set_device_frequency(&mut self, device_name: &String, frequency: u32) -> Result<(), Error> {
        let device = match self.fetch_device_info(device_name) {
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
        let device = match self.fetch_device_info(device_name) {
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
        let device = match self.fetch_device_info(device_name) {
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