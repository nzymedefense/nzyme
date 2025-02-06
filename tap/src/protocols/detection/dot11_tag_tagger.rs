use crate::protocols::detection::taggers::dot11::remote_id_tagger;

pub fn tag_advertisement_frame_tags(data: &[u8]) -> Option<()> {
    remote_id_tagger::tag(data)
}