use std::collections::HashMap;
use crossbeam_channel::{Receiver, Sender};
use log::error;
use crate::wireless::dot11::sona::commands::{AddressedSonaCommand, SonaCommand};

pub struct SonaCommandRouter {
    receiver: Receiver<AddressedSonaCommand>,
    pub sender: Sender<AddressedSonaCommand>,
    capture_senders: HashMap<String, Sender<SonaCommand>>,
}

impl SonaCommandRouter {
    pub fn new() -> Self {
        let (tx, rx) = crossbeam_channel::bounded::<AddressedSonaCommand>(128);

        Self {
            receiver: rx,
            sender: tx,
            capture_senders: HashMap::new(),
        }
    }

    pub fn register_capture(&mut self, serial: &str) -> Receiver<SonaCommand> {
        let (tx, rx) = crossbeam_channel::bounded::<SonaCommand>(128);
        self.capture_senders.insert(serial.to_string(), tx);
        rx
    }

    pub fn run(&self) {
        while let Ok(msg) = self.receiver.recv() {
            if let Some(tx) = self.capture_senders.get(&msg.sona_device_serial) {
                if let Err(e) = tx.send(msg.cmd) {
                    error!( "Failed to route command to Sona [{}]: {}", msg.sona_device_serial, e);
                }
            } else {
                error!("Cannot route command to unknown Sona [{}].",msg.sona_device_serial);
            }
        }
    }
}
