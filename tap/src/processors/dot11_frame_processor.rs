use std::sync::{Arc, Mutex};

use log::{trace, error};

use crate::{dot11::{frames::{Dot11Frame, FrameSubType, Dot11BeaconFrame, Dot11DataFrame, Dot11DeauthenticationFrame, Dot11ProbeRequestFrame}, parsers::{management::{beacon_frame_parser, deauthentication_frame_parser, probe_request_frame_parser}, data::data_frame_parser}}, data::dot11_table::Dot11Table};

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
                    Ok(frame) => self.handle_beacon_frame(frame),
                    Err(e) => trace!("Could not parse beacon frame: {}", e)
                }
            },
            FrameSubType::Deauthentication => {
                match deauthentication_frame_parser::parse(frame) {
                    Ok(frame) => self.handle_deauthentication_frame(frame),
                    Err(e) => trace!("Could not parse deauthentication frame: {}", e)
                }
            },
            FrameSubType::ProbeRequest => {
                match probe_request_frame_parser::parse(frame) {
                    Ok(frame) => self.handle_probe_request_frame(frame),
                    Err(e) => trace!("Could not parse probe response frame: {}", e)
                }
            },
            FrameSubType::Data |
            FrameSubType::QosData |
            FrameSubType::QosDataCfAck |
            FrameSubType::QosDataCfPoll |
            FrameSubType::QosDataCfAckCfPoll => {
                match data_frame_parser::parse(frame) {
                    Ok(frame) => self.handle_data_frame(frame),
                    Err(e) => trace!("Could not parse data frame: {}", e)
                }
            },
            FrameSubType::AssociationRequest |
            FrameSubType::AssociationResponse |
            FrameSubType::ReAssociationRequest |
            FrameSubType::ReAssociationResponse |
            FrameSubType::ProbeResponse |
            FrameSubType::TimingAdvertisement |
            FrameSubType::Atim |
            FrameSubType::Disassocation |
            FrameSubType::Authentication |
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
            FrameSubType::Null |
            FrameSubType::QosNull |
            FrameSubType::QosCfPoll |
            FrameSubType::QosCfAckCfPoll |
            FrameSubType::DmgBeacon |
            FrameSubType::S1gBeacon |
            FrameSubType::Reserved |
            FrameSubType::Invalid => { /* ignored */ }
        }
    }

    fn handle_beacon_frame(&self, frame: Dot11BeaconFrame) {
        match self.dot11_table.lock() {
            Ok(mut table) => table.register_beacon_frame(frame),
            Err(e) => error!("Could not acquire 802.11 table lock: {}", e)
        }
    }

    fn handle_probe_request_frame(&self, frame: Dot11ProbeRequestFrame) {
        match self.dot11_table.lock() {
            Ok(table) => table.register_probe_request_frame(frame),
            Err(e) => error!("Could not acquire 802.11 table lock: {}", e)
        }
    }

    fn handle_data_frame(&self, frame: Dot11DataFrame) {
        match self.dot11_table.lock() {
            Ok(table) => table.register_data_frame(frame),
            Err(e) => error!("Could not acquire 802.11 table lock: {}", e)
        }
    }

    fn handle_deauthentication_frame(&self, _: Dot11DeauthenticationFrame) {

    }

}