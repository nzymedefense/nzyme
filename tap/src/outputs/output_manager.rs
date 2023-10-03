use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use std::thread;
use std::time::Duration;
use log::{error, info};
use uuid::Uuid;
use crate::dot11::frames::Dot11BeaconFrame;
use crate::ethernet::packets::DNSPacket;
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
        let os_output = Box::new(OpenSearchOutput::new()) as Box<dyn OutputMessageReceiver + Send>;
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
                let beacons: Vec<Arc<Dot11BeaconFrame>> = bus.dot11_beacon_output_pipeline.receiver.try_iter().collect();
                let dns_packets: Vec<Arc<DNSPacket>> = bus.dns_output_pipeline.receiver.try_iter().collect();

                match outputs.lock() {
                    Ok(outputs) => {
                        for output in outputs.values() {
                            output.write_dot11_beacon_frames(&beacons);
                            output.write_dns_packets(&dns_packets);
                        }
                    },
                    Err(e) => error!("Could not acquire output configuration mutex: {}", e)
                }

                thread::sleep(Duration::from_millis(500))
            }
        });
    }

}