use std::sync::Arc;

use crate::dot11::{frames::{Dot11Frame, FrameSubType}, parsers::management::beacon_frame_parser};

pub struct Dot11FrameProcessor {

}

impl Dot11FrameProcessor {

    pub fn process(&mut self, frame: &Arc<Dot11Frame>) {
        match frame.frame_type {
            FrameSubType::Beacon => {
                beacon_frame_parser::parse(frame);
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

}
