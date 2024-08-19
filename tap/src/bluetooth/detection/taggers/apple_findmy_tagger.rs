use std::any::Any;
use std::collections::HashMap;
use std::sync::Arc;
use log::info;
use crate::bluetooth::bluetooth_device_advertisement::BluetoothDeviceAdvertisement;
use crate::bluetooth::detection::taggers::tagger_utils;

pub fn tag(advertisement: &Arc<BluetoothDeviceAdvertisement>) -> Option<HashMap<String, Box<dyn Any>>> {
    if tagger_utils::mac_is_locally_administered_and_multicast(&advertisement.mac)
        && advertisement.company_id.is_some()
        && advertisement.company_id? == 76
        && advertisement.manufacturer_data.is_some() {

        let payload: Vec<u8> = advertisement.as_ref().manufacturer_data.clone()?;

        if payload.len() < 3 {
            return None
        }

        if payload[0] == 0x07 || payload[0] == 0x12 {
            let mut parameters: HashMap<String, Box<dyn Any>> = HashMap::new();

            let (battery_level, battery_level_string) = status_to_battery_state_description(
                (payload[2] & 0xF0) >> 4
            );

            parameters.insert("of_type".to_string(), Box::new(payload[0]));
            parameters.insert("status".to_string(), Box::new(payload[2]));
            parameters.insert("device_type".to_string(), Box::new(payload[2] & 0x0F));
            parameters.insert("battery_level".to_string(), Box::new(battery_level));
            parameters.insert("battery_level_string".to_string(), Box::new(battery_level_string));

            Some(parameters)
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