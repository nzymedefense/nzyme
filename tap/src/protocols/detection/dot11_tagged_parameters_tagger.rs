use std::sync::{Arc, Mutex};
use log::error;
use crate::helpers::timer::{record_timer, Timer};
use crate::messagebus::bus::Bus;
use crate::messagebus::channel_names::GenericChannelName;
use crate::metrics::Metrics;
use crate::protocols::detection::taggers::remoteid::uav_remote_id_tagger;
use crate::to_pipeline;
use crate::wireless::tags::Tag;

pub fn tag_advertisement_frame_tags(data: &[u8],
                                    bssid: String,
                                    rssi: Option<i8>,
                                    frequency: Option<u16>,
                                    bus: Arc<Bus>,
                                    metrics: Arc<Mutex<Metrics>>) -> Vec<Tag> {
    let mut tags = Vec::new();

    // UAV Remote ID.
    let mut remote_id_timer = Timer::new();
    if let Some(rid) = uav_remote_id_tagger::tag(data, bssid, rssi, frequency) {
        remote_id_timer.stop();
        record_timer(
            remote_id_timer.elapsed_microseconds(),
            "generic.tagging.uav_remote_id.tagged",
            &metrics
        );

        let len = rid.estimate_struct_size();
        to_pipeline!(
            GenericChannelName::UavRemoteIdPipeline,
            bus.uav_remote_id_pipeline.sender,
            Arc::new(rid),
            len
        );

        tags.push(Tag::UavRemoteId);
    }
    
    tags
}