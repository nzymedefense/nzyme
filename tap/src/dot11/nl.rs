use std::collections::{HashMap, HashSet};

use anyhow::{Error, bail};

use neli::{consts::{
    nl::{NlmF},
    socket::NlFamily,
    rtnl::{Arphrd, RtAddrFamily, Rtm},
}, genl::{Genlmsghdr, GenlmsghdrBuilder, NoUserHeader, NlattrBuilder, AttrTypeBuilder}, nl::{NlPayload}, rtnl::{IfinfomsgBuilder}, types::Buffer};
use neli::attr::Attribute;
use neli::consts::nl::{GenlId};
use neli::err::DeError;
use neli::genl::Nlattr;
use neli::types::NlBuffer;
use crate::dot11::nl_consts::{Nl80211ChanWidthAttr, NL_80211_GENL_NAME, NL_80211_GENL_VERSION};
use crate::dot11::nl_skinny_router::NlSkinnyRouter;
use crate::dot11::supported_frequency::{SupportedChannelWidth, SupportedFrequency};

use super::nl_consts::{Nl80211Attribute, Nl80211Command, Nl80211BandAttr, Nl80211FrequencyAttr, Nl80211MntrFlags};

#[derive(Debug)]
pub struct Interface {
    pub name: String,
    pub phy_index: u32,
    pub if_index: u32,
}

#[derive(Debug)]
struct PhyResponse {
    phy_index: u32,
    supported_frequencies: Vec<SupportedFrequency>
}

#[derive(Debug, Clone)]
pub struct InterfaceInfo {
    pub name: String,
    pub if_index: u32,
    pub supported_frequencies: Vec<SupportedFrequency>
}

pub enum InterfaceState {
    Up, Down
}

// Parse specific attributes from an 802.11 (wireless) related Netlink message
// and return a structured response with information about a network interface,
// such as its name, physical layer index, and interface index
fn handle_interface_response(msgs: NlBuffer<GenlId, Genlmsghdr<Nl80211Command, Nl80211Attribute>>) -> Result<HashMap<String, Interface>, DeError> {
    let mut phys_found: HashSet<u32> = HashSet::new();
    let mut names_found: HashMap<u32, String> = HashMap::new();
    let mut if_indexes: HashMap<u32, u32> = HashMap::new();

    for msg in msgs {
        if let NlPayload::Payload(payload) = msg.nl_payload() {
            let mut phy_index: Option<u32> = None;
            let mut name: Option<String> = None;
            let mut if_index: Option<u32> = None;

            for attr in payload.attrs().iter() {
                match attr.nla_type().nla_type() {
                    Nl80211Attribute::WiPhy => {
                        phy_index = Some(attr.get_payload_as()?);
                    },
                    Nl80211Attribute::IfIndex => {
                        if_index = Some(attr.get_payload_as()?);
                    },
                    Nl80211Attribute::IfName => {
                        name = Some(attr.get_payload_as_with_len::<String>()?);
                    },
                    _ => {}
                }
            }

            // we will always see a phy index, but we may not see the other attributes on the same message
            if phy_index.is_some() {
                phys_found.insert(phy_index.unwrap());

                // check if we also saw the if_index on this message batch
                if if_index.is_some() {
                    if_indexes.insert(phy_index.unwrap(), if_index.unwrap());
                }
                // check if we also saw the name on this message batch
                if name.is_some() {
                    names_found.insert(phy_index.unwrap(), name.unwrap());
                }
            }
        }
    }

    // now lets compile all the information we found regarding the interfaces, and if we have all attributes we will return them
    let mut interfaces = HashMap::new();

    for phy_index in phys_found {
        let name = names_found.get(&phy_index);
        let if_index = if_indexes.get(&phy_index);

        // If any of the required attributes is missing, return an error.
        if name.is_none() || if_index.is_none() {
            return Err(DeError::new("Missing necessary attributes"));
        }

        interfaces.insert(name.unwrap().clone(), Interface {
            name: name.unwrap().clone(),
            phy_index,
            if_index: if_index.unwrap().clone(),
        });
    }

    Ok(interfaces)
}

fn handle_phy_response(msgs: NlBuffer<GenlId, Genlmsghdr<Nl80211Command, Nl80211Attribute>>) -> Result<PhyResponse, DeError> {

    let mut phy_index: Option<u32> = None;
    let mut supported_frequencies: Vec<SupportedFrequency> = Vec::new();

    for msg in msgs {
        if let NlPayload::Payload(payload) = msg.nl_payload() {
            for attr in payload.attrs().iter() {
                match attr.nla_type().nla_type() {
                    Nl80211Attribute::WiPhy => {
                        phy_index = Some(attr.get_payload_as().map_err(|_| DeError::new("Failed to get payload as u32"))?);
                    },
                    Nl80211Attribute::WiPhyBands => {
                        // bands is an array, get the handle for the array
                        let bands_handle = attr.get_attr_handle::<Nl80211BandAttr>().map_err(|_| DeError::new("Failed to get WiPhyBands attribute handle"))?;
                        for bands in bands_handle.iter() {
                            // for each element of the array now we get the nested attributes
                            let band_handle = bands.get_attr_handle().map_err(|_| DeError::new("Failed to get WiPhyBands attribute handle"))?;

                            for band_attr in band_handle.get_attrs() {
                                #[allow(clippy::single_match)]
                                match band_attr.nla_type().nla_type() {
                                    Nl80211BandAttr::Freqs => {
                                        // This is an array. Get the handle for the array.
                                        let freqs_handle = band_attr.get_attr_handle::<Nl80211FrequencyAttr>().map_err(|_| DeError::new("Failed to get WiPhyFreq attribute handle"))?;

                                        for freq in freqs_handle.iter() {
                                            // Get nested attributes of each element in the band array.
                                            let freq_handle = freq.get_attr_handle().map_err(|_| DeError::new("Failed to get WiPhyFreq attribute handle"))?;
                                            let freq_attr = freq_handle.get_attribute(Nl80211FrequencyAttr::Freq);
                                            let disabled_attr = freq_handle.get_attribute(Nl80211FrequencyAttr::Disabled);
                                            let no20_attr = freq_handle.get_attribute(Nl80211FrequencyAttr::No20Mhz);
                                            let noht40minus_attr = freq_handle.get_attribute(Nl80211FrequencyAttr::NoHt40Minus);
                                            let noht40plus_attr = freq_handle.get_attribute(Nl80211FrequencyAttr::NoHt40Plus);
                                            let no80_attr = freq_handle.get_attribute(Nl80211FrequencyAttr::No80Mhz);
                                            let no160_attr = freq_handle.get_attribute(Nl80211FrequencyAttr::No160Mhz);
                                            let no320_attr = freq_handle.get_attribute(Nl80211FrequencyAttr::No320Mhz);

                                            // Skip disabled interfaces.
                                            if disabled_attr.is_some() || freq_attr.is_none() {
                                                continue;
                                            }

                                            let mut channel_widths: Vec<SupportedChannelWidth>  = Vec::new();

                                            if no20_attr.is_none() {
                                                channel_widths.push(SupportedChannelWidth::Mhz20)
                                            }

                                            if noht40minus_attr.is_none() {
                                                channel_widths.push(SupportedChannelWidth::Mhz40Minus)
                                            }

                                            if noht40plus_attr.is_none() {
                                                channel_widths.push(SupportedChannelWidth::Mhz40Plus)
                                            }

                                            if no80_attr.is_none() {
                                                channel_widths.push(SupportedChannelWidth::Mhz80)
                                            }

                                            if no160_attr.is_none() {
                                                channel_widths.push(SupportedChannelWidth::Mhz160)
                                            }

                                            if no320_attr.is_none() {
                                                channel_widths.push(SupportedChannelWidth::Mhz320)
                                            }

                                            let frequency: u32 = freq_attr.unwrap().get_payload_as()?;
                                            supported_frequencies.push(
                                                SupportedFrequency {
                                                    frequency,
                                                    channel_widths
                                                }
                                            );
                                        }
                                    },
                                    _ => {}
                                }
                            }
                        }
                    },
                    _ => {}
                }
            }
        }
    }

    if phy_index.is_none() {
        return Err(DeError::new("Missing necessary attributes"));
    }

    Ok(PhyResponse {
        phy_index: phy_index.unwrap(),
        supported_frequencies
    })
}

pub struct Nl {
    dot11_socket: NlSkinnyRouter,
    dot11_family_id: u16,
    rt_socket: NlSkinnyRouter,
    devices: HashMap<String, InterfaceInfo>
}

impl Nl {

    pub fn new() -> Result<Self, Error> {

        let mut dot11_socket = match NlSkinnyRouter::connect(NlFamily::Generic) {
            Ok(sock) => sock,
            Err(e) => bail!("Could not open Netlink 802.11 socket: {}", e)
        };

        let dot11_family_id = match dot11_socket.resolve_genl_family(NL_80211_GENL_NAME) {
            Ok(family_id) => family_id,
            Err(e) => bail!("Could not resolve Netlink family: {}", e)
        };

        let rt_socket = match NlSkinnyRouter::connect(NlFamily::Route) {
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

    // Fetch all available 802.11 devices, with interface WiPhy id and name
    pub fn fetch_devices(&mut self) -> Result<HashMap<String, Interface>, Error> {
        let get_if_payload = match GenlmsghdrBuilder::<Nl80211Command, Nl80211Attribute, NoUserHeader>::default()
            .cmd(Nl80211Command::GetIf)
            .version(0)
            .build() {
            Ok(pl) => pl,
            Err(e) => bail!("Could not build GetIf Netlink payload: {}", e)
        };

        self.dot11_socket.send(self.dot11_family_id, NlmF::DUMP, NlPayload::Payload(get_if_payload))?;

        let mut interfaces: HashMap<String, Interface> = HashMap::new();

        let buffer = self.dot11_socket.recv();
        match buffer {
            Some(buf) => {
                match buf {
                    Ok(b) => {
                        let response = handle_interface_response(b);
                        match response {
                            Ok(ifaces) => interfaces.extend(ifaces),
                            Err(e) => bail!("Could not parse GetIf Netlink response: {}", e)
                        }
                    },
                    Err(e) => {
                        bail!("Could not receive GetIf Netlink response: {}", e);
                    }
                }
            },
            None => {
                bail!("Could not receive GetIf Netlink response");
            }
        }

        Ok(interfaces)
    }

    pub fn fetch_device_info(&mut self, device_name: &String) -> Result<InterfaceInfo, Error> {
        // get the device phy index
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

        // build the Nlattr to filter the dump by phy index
        let mut attrs: Vec<Nlattr<Nl80211Attribute, Buffer>> = vec![];

        // filter by the phy number
        let attr_filter_wiphy_dump_type = match AttrTypeBuilder::default().nla_type(Nl80211Attribute::WiPhy).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct WiPhy Netlink attribute type: {}", e)
        };

        let attr_filter_wiphy_dump = match NlattrBuilder::default().nla_type(attr_filter_wiphy_dump_type)
            .nla_payload(interface_info.phy_index).build() {
            Ok(attr) => attr,
            Err(e) => bail!("Could not construct WiPhy Netlink attribute: {}", e)
        };
        attrs.push(attr_filter_wiphy_dump);

        // inform we want a split dump
        let attr_filter_split_dump_type = match AttrTypeBuilder::default().nla_type(Nl80211Attribute::SplitWiphyDump).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct SplitWiphyDump Netlink attribute type: {}", e)
        };
        let attr_filter_split_dump = match NlattrBuilder::default().nla_type(attr_filter_split_dump_type)
            .nla_payload(Buffer::from(vec![])).build() {
            Ok(attr) => attr,
            Err(e) => bail!("Could not construct SplitWiphyDump Netlink attribute: {}", e)
        };
        attrs.push(attr_filter_split_dump);

        let get_wiphy = match GenlmsghdrBuilder::<Nl80211Command, Nl80211Attribute, NoUserHeader>::default()
            .cmd(Nl80211Command::GetWiPhy)
            .version(0)
            .attrs(attrs.into_iter().collect())
            .build() {
            Ok(pl) => pl,
            Err(e) => bail!("Could not build GetWiPhy Netlink payload: {}", e)
        };

        self.dot11_socket.send(self.dot11_family_id, NlmF::DUMP | NlmF::ACK, NlPayload::Payload(get_wiphy))?;

        let buffer = self.dot11_socket.recv();
        match buffer {
            Some(buf) => {
                match buf {
                    Ok(b) => {
                        let mut phys: HashMap<u32, PhyResponse> = HashMap::new();

                        let phy = handle_phy_response(b);
                        match phy {
                            Ok(phy) => { phys.insert(phy.phy_index, phy); },
                            Err(e) => bail!("Could not parse GetWiPhy Netlink response: {}", e)
                        };

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
                    },
                    Err(e) => {
                        bail!("Could not receive GetWiPhy Netlink response: {}", e);
                    }
                }
            },
            None => {
                bail!("Could not receive GetWiPhy Netlink response");
            }
        }


    }

    pub fn set_device_frequency(&mut self, device_name: &String, frequency: u32, width: &SupportedChannelWidth)
        -> Result<(), Error> {
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

        let attr_iwiphy_freq = match NlattrBuilder::default().nla_type(attr_wiphy_freq_type)
            .nla_payload(frequency).build() {
            Ok(attr) => attr,
            Err(e) => bail!("Could not construct WiPhyFreq Netlink attribute: {}", e)
        };

        let (nl_width, center_frequency) = match width {
            SupportedChannelWidth::Mhz20 => (Nl80211ChanWidthAttr::Cw20, frequency),
            SupportedChannelWidth::Mhz40Minus => {
                (
                    Nl80211ChanWidthAttr::Cw40,
                    (frequency+(frequency + 20)) / 2
                )
            },
            SupportedChannelWidth::Mhz40Plus => {
                (
                    Nl80211ChanWidthAttr::Cw40,
                    (frequency + (frequency - 20)) / 2
                )
            },
            SupportedChannelWidth::Mhz80 => {
                (
                    Nl80211ChanWidthAttr::Cw80,
                    (frequency+(frequency + 60)) / 2
                )
            },
            SupportedChannelWidth::Mhz160 => {
                (
                    Nl80211ChanWidthAttr::Cw160,
                    (frequency+(frequency + 140)) / 2
                )
            },
            SupportedChannelWidth::Mhz320 => {
                (
                    Nl80211ChanWidthAttr::Cw320,
                    (frequency+(frequency + 300)) / 2
                )
            }
        };

        let attr_channel_width_type = match AttrTypeBuilder::default().nla_type(Nl80211Attribute::ChannelWidth).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct ChannelWidth Netlink attribute type: {}", e)
        };

        let attr_channel_width = match NlattrBuilder::default().nla_type(attr_channel_width_type)
            .nla_payload(nl_width).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct ChannelWidth Netlink attribute: {}", e)
        };

        let attr_center_frequency_type = match AttrTypeBuilder::default().nla_type(Nl80211Attribute::CenterFreq1).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct CenterFreq1 Netlink attribute type: {}", e)
        };

        let attr_center_frequency = match NlattrBuilder::default().nla_type(attr_center_frequency_type)
            .nla_payload(center_frequency).build() {
            Ok(t) => t,
            Err(e) => bail!("Could not construct CenterFreq1 Netlink attribute: {}", e)
        };

        let payload = match GenlmsghdrBuilder::<Nl80211Command, Nl80211Attribute, NoUserHeader>::default()
            .cmd(Nl80211Command::SetChannel)
            .version(NL_80211_GENL_VERSION)
            .attrs(vec![attr_if_index, attr_iwiphy_freq, attr_channel_width, attr_center_frequency].into_iter().collect())
            .build() {
            Ok(p) => p,
            Err(e) => bail!("Could not construct WiPhyFreq Netlink command payload: {}", e)
        };

        self.dot11_socket.send(self.dot11_family_id, NlmF::empty(), NlPayload::Payload(payload))?;

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

        self.dot11_socket.send(self.dot11_family_id, NlmF::REQUEST | NlmF::ACK, NlPayload::Payload(payload))?;
        match self.dot11_socket.recv::<u16, Buffer>() {
            Some(Ok(_)) => {},
            Some(Err(e)) => bail!("Could not set monitor mode. Netlink response: {}", e),
            None => {}
        }
        /*let _: NlRouterReceiverHandle<GenlId, Genlmsghdr<Nl80211Command, Nl80211Attribute>> =
            match self.dot11_socket.send(self.dot11_family_id, NlmF::REQUEST | NlmF::ACK, NlPayload::Payload(payload)){
                Ok(recv) => recv,
                Err(e) => bail!("Could not send WiPhyFreq Netlink command: {}", e)
            };*/

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

        self.rt_socket.send(
            Rtm::Setlink,
            NlmF::ROOT | NlmF::ECHO,
            NlPayload::Payload(msg))?;
//        match self.rt_socket.recv::<u16, Buffer>() {
//            Some(Ok(_)) => {},
//            Some(Err(e)) => bail!("Could not set monitor mode. Netlink response: {}", e),
//            None => {}
//        }
        /*let _: NlRouterReceiverHandle<GenlId, Nlmsghdr<Rtm, Ifinfomsg>> =
            match self.rt_socket.send(
                Rtm::Setlink,
                NlmF::ROOT | NlmF::ECHO,
                NlPayload::Payload(msg)) {
                Ok(recv) => recv,
                Err(e) => { bail!("Could not send request: {}", e); }
            };*/

        Ok(())
    }

}