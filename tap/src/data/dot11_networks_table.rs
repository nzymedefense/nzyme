use std::{sync::Mutex, collections::HashMap};

pub struct Dot11NetworksTable {
    ssids: Mutex<HashMap<String, SsidStatistics>>
}

pub struct SsidStatistics {

}

impl Dot11NetworksTable {

    pub fn new() -> Self {
        Self {
            ssids: Mutex::new(HashMap::new())
        }
    }

}