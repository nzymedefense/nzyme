use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use std::thread;
use log::{error, info};
use uuid::Uuid;
use crate::messagebus::bus::Bus;
use crate::outputs::targets::opensearch::opensearch_output::OpenSearchOutput;
use crate::outputs::targets::output_message_receiver::OutputMessageReceiver;

pub struct OutputManager {
    bus: Arc<Bus>,
    active_outputs: Arc<Mutex<HashMap<Uuid, Box<dyn OutputMessageReceiver + Send>>>>
}

impl OutputManager {

    pub fn new(bus: Arc<Bus>) -> Self {
        let active_outputs = Arc::new(Mutex::new(HashMap::new()));
        let os_output = Box::new(OpenSearchOutput{}) as Box<dyn OutputMessageReceiver + Send>;
        active_outputs.lock().unwrap().insert(Uuid::new_v4(), os_output);

        OutputManager {
            bus,
            active_outputs
        }
    }

    pub fn spawn(&self) {
        info!("Subscribing output manager processing to message output channel.");

        let bus = self.bus.clone();
        let outputs = self.active_outputs.clone();
        thread::spawn(move || {
            loop {
                // 802.11 Beacon frames.
                while let Some(beacon) = bus.dot11_beacon_pipeline.receiver.iter().next() {
                    match outputs.lock() {
                        Ok(outputs) => {
                            for output in outputs.values() {
                                output.write_dot11_beacon_frame(&beacon);
                            }
                        },
                        Err(e) => error!("Could not acquire output configuration mutex: {}", e)
                    }
                }

                // 802.11 XXX frames etc
            }
        });
    }

}