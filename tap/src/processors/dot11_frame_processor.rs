use std::sync::{Arc, Mutex};

use log::trace;

use crate::{dot11::{frames::{Dot11Frame, FrameSubType, Dot11BeaconFrame}, parsers::management::beacon_frame_parser}, data::dot11_networks_table::Dot11NetworksTable};

pub struct Dot11FrameProcessor {
    networks_table: Arc<Mutex<Dot11NetworksTable>>
}

impl Dot11FrameProcessor {

    pub fn new(networks_table: Arc<Mutex<Dot11NetworksTable>>) -> Self {
        Self {
            networks_table
        }
    }

    pub fn process(&self, frame: &Arc<Dot11Frame>) {
        match frame.frame_type {
            FrameSubType::Beacon => {
                match beacon_frame_parser::parse(frame) {
                    Ok(beacon) => self.handle_beacon(beacon),
                    Err(e) => trace!("Could not parse beacon frame: {}", e)
                }
            }

            FrameSubType::AssociationRequest |
            FrameSubType::AssociationResponse |
            FrameSubType::ReAssociationRequest |
            FrameSubType::ReAssociationResponse |
            FrameSubType::ProbeRequest |
            FrameSubType::ProbeResponse |
            FrameSubType::TimingAdvertisement |
            FrameSubType::Atim |
            FrameSubType::Disassocation |
            FrameSubType::Authentication |
            FrameSubType::Deauthentication |
            FrameSubType::Action |
            FrameSubType::ActionNoAck |
            FrameSubType::Trigger |
            FrameSubType::Tack |
            FrameSubType::BeamformingReportPoll |
            FrameSubType::VhtHeNdpAnnouncement |
            FrameSubType::ControlFrameExtension |
            FrameSubType::ControlWrapper |
            FrameSubType::BlockAckRequest |
            FrameSubType::BlockAck |
            FrameSubType::PsPoll |
            FrameSubType::Rts |
            FrameSubType::Cts |
            FrameSubType::Ack |
            FrameSubType::CfEnd |
            FrameSubType::CfEndCfAck |
            FrameSubType::Data |
            FrameSubType::Null |
            FrameSubType::QosData |
            FrameSubType::QosDataCfAck |
            FrameSubType::QosDataCfPoll |
            FrameSubType::QosDataCfAckCfPoll |
            FrameSubType::QosNull |
            FrameSubType::QosCfPoll |
            FrameSubType::QosCfAckCfPoll |
            FrameSubType::DmgBeacon |
            FrameSubType::S1gBeacon |
            FrameSubType::Reserved |
            FrameSubType::Invalid => { /* ignored */ }
        }
    }

    fn handle_beacon(&self, beacon: Dot11BeaconFrame) {
        todo!()
    }    

}