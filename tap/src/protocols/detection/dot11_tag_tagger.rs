use log::info;
use crate::protocols::detection::taggers::remoteid::remote_id_tagger;

pub fn tag_advertisement_frame_tags(data: &[u8]) -> Option<()> {
    let rid = remote_id_tagger::tag(data);

    info!("RID: {:?}", rid);
    
    None
}