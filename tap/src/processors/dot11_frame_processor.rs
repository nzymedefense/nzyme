use std::sync::{Arc, Mutex};

use log::{trace, error};

use crate::{dot11::{frames::{Dot11Frame, FrameSubType, Dot11BeaconFrame}, parsers::management::beacon_frame_parser}, data::dot11_table::Dot11Table};

pub struct Dot11FrameProcessor {
    dot11_table: Arc<Mutex<Dot11Table>>
}

impl Dot11FrameProcessor {

    pub fn new(dot11_table: Arc<Mutex<Dot11Table>>) -> Self {
        Self {
            dot11_table
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
        match self.dot11_table.lock() {
            Ok(mut table) => table.register_beacon_frame(beacon),
            Err(e) => error!("Could not acquire 802.11 table lock: {}", e)
        }
    }    

}