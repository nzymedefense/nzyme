use std::any::Any;
use std::collections::HashMap;
use std::sync::Arc;
use crate::bluetooth::bluetooth_device_advertisement::BluetoothDeviceAdvertisement;
use crate::bluetooth::detection::taggers::apple_findmy_tagger;

pub fn tag_device_advertisement(advertisement: &Arc<BluetoothDeviceAdvertisement>)
                                -> Option<Vec<HashMap<String, Box<dyn Any>>>> {
    let mut tags: Vec<HashMap<String, Box<dyn Any>>> = Vec::new();

    if let Some(t) = apple_findmy_tagger::tag(advertisement) {
        tags.push(t);
    }

    if tags.is_empty() {
        None
    } else {
        Some(tags)
    }
}