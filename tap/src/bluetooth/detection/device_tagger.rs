use std::collections::HashMap;
use std::sync::Arc;
use crate::bluetooth::bluetooth_device_advertisement::BluetoothDeviceAdvertisement;
use crate::bluetooth::detection::taggers::apple_findmy_tagger;

#[derive(Debug, Clone)]
pub enum TagValue {
    Byte(u8),
    Text(String),
    Boolean(bool)
}

pub fn tag_device_advertisement(advertisement: &Arc<BluetoothDeviceAdvertisement>)
                                -> Option<HashMap<String, HashMap<String, TagValue>>> {
    let mut tags: HashMap<String, HashMap<String, TagValue>> = HashMap::new();

    if let Some((tag, params)) = apple_findmy_tagger::tag(advertisement) {
        tags.insert(tag, params);
    }

    if tags.is_empty() {
        None
    } else {
        Some(tags)
    }
}