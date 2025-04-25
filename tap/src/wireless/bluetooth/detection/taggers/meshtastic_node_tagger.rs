use std::collections::HashMap;
use std::sync::Arc;
use crate::wireless::bluetooth::bluetooth_device_advertisement::BluetoothDeviceAdvertisement;
use crate::wireless::bluetooth::detection::device_tagger::TagValue;

pub fn tag(advertisement: &Arc<BluetoothDeviceAdvertisement>) 
    -> Option<(String, HashMap<String, TagValue>)> {
    
    advertisement.uuids.as_ref()?;
    let name = &advertisement.name;
    
    for uuid in advertisement.uuids.clone().unwrap() {
        if uuid.to_lowercase().eq("6ba1b218-15a8-461f-9fa8-5dcae273eafd") {
            let mut parameters: HashMap<String, TagValue> = HashMap::new();
            parameters.insert("node_name".to_string(), 
                              TagValue::Text(name.clone().unwrap_or("Unknown".to_string())));
            return Some(("meshtastic_node".to_string(), parameters))
        }
    }
    
    None
}