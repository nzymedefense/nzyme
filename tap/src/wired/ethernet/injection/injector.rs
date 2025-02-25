use std::rc::Rc;
use std::sync::Mutex;
use anyhow::{bail, Error};
use log::debug;
use pcap::{Active, Capture, Device};

pub struct Injector {
    device: Rc<Mutex<Device>>,
    capture: Rc<Mutex<Option<Capture<Active>>>>,
    device_name: String
}

impl Injector {

    pub fn new(device_name: String) -> Result<Self, Error> {
        let device = match Device::list()?.into_iter().find(|dev| dev.name == device_name) {
            Some(device) => device,
            None => bail!("Device [{}] not found.", device_name)
        };

        Ok(Self { device: Rc::new(Mutex::new(device)), capture: Rc::new(Mutex::new(None)), device_name })
    }

    pub fn open(&mut self) -> Result<(), Error> {
        match self.device.lock() {
            Ok(device) => {
                match device.clone().open() {
                    Ok(capture) => {
                        match self.capture.lock() {
                            Ok(mut ch) => {
                                *ch = Some(capture);
                                Ok(())
                            },
                            Err(e) => bail!("Could not acquire capture mutex: {}", e),
                        }
                    },
                    Err(e) => bail!("Could not open device: {}", e)
                }
            },
            Err(e) => bail!("Could not acquire device mutex: {}", e)
        }
    }

    pub fn inject(&mut self, data: Vec<u8>) -> Result<(), Error> {
        let data_length = data.len();
        match self.capture.lock() {
            Ok(mut c) => {
                match *c { Some(ref mut capture) => {
                    match capture.sendpacket(data) {
                        Ok(()) => {
                            debug!("Injected <{}> bytes into [{}]", data_length, self.device_name);
                            Ok(())
                        },
                        Err(e) => bail!("Could not inject <{}> bytes into [{}]: {}",
                            data_length, self.device_name, e)
                    }


                } _ => {
                    bail!("Injector is not open. You must call `open()` first.")
                }}
            },
            Err(e) => bail!("Could not acquire capture mutex: {}", e)
        }
    }

}