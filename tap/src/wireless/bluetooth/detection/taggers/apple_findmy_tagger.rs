use std::collections::HashMap;
use std::sync::Arc;
use crate::wireless::bluetooth::bluetooth_device_advertisement::BluetoothDeviceAdvertisement;
use crate::wireless::bluetooth::detection::device_tagger::TagValue;
use crate::wireless::bluetooth::detection::taggers::tagger_utils;

pub fn tag(advertisement: &Arc<BluetoothDeviceAdvertisement>) -> Option<(String, HashMap<String, TagValue>)> {
    // We check for locally administered OR multicast because some AirTags are using only multicast.
    if tagger_utils::mac_is_locally_administered_or_multicast(&advertisement.mac)
        && advertisement.company_id.is_some()
        && advertisement.company_id? == 76
        && advertisement.manufacturer_data.is_some() {

        let payload: Vec<u8> = advertisement.as_ref().manufacturer_data.clone()?;

        if payload.len() < 3 {
            return None
        }

        if payload[0] == 0x07 || payload[0] == 0x12 {
            let mut parameters: HashMap<String, TagValue> = HashMap::new();

            let (battery_level, battery_level_string) = status_to_battery_state_description(
                (payload[2] & 0xF0) >> 4
            );

            let is_paired = payload[0] == 0x12;

            // Type and paired are reliable but all others are not. We are not reporting those yet.
            parameters.insert("of_type".to_string(), TagValue::Byte(payload[0]));
            parameters.insert("status".to_string(), TagValue::Byte(payload[2]));
            parameters.insert("paired".to_string(), TagValue::Boolean(is_paired));
            parameters.insert("device_type".to_string(), TagValue::Byte(payload[2] & 0x0F));
            parameters.insert("battery_level".to_string(), TagValue::Byte(battery_level));
            parameters.insert("battery_level_string".to_string(),
                              TagValue::Text(battery_level_string.to_string()));

            let tag = match is_paired  {
                true => "apple_find_my_paired",
                false => "apple_find_my_unpaired"
            }.to_string();

            Some((tag, parameters))
        } else {
            None
        }
    } else {
        None
    }
}

fn status_to_battery_state_description(state: u8) -> (u8, &'static str) {
    match state {
        1 => (90, "Fully Charged"),
        2 => (80, "Very High"),
        3 => (70, "High"),
        4 => (60, "Above Medium"),
        5 => (50, "Medium"),
        6 => (40, "Below Medium"),
        7 => (30, "Low"),
        8 => (20, "Very Low"),
        9 => (10, "Critically Low"),
        0 => (1, "Almost Empty"),
        _ => (0, "Unknown"),
    }
}