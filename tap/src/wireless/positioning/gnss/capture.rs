use std::io::{BufRead, BufReader};
use std::sync::{Arc, Mutex};
use std::time::Duration;
use chrono::Utc;
use log::{debug, error, info, trace};
use crate::messagebus::bus::Bus;
use crate::messagebus::channel_names::GenericChannelName;
use crate::metrics::Metrics;
use crate::to_pipeline;
use crate::wireless::positioning::gnss::gnss_constellation::GNSSConstellation;
use crate::wireless::positioning::nmea::nmea_message::NMEAMessage;

pub struct Capture {
    pub metrics: Arc<Mutex<Metrics>>,
    pub bus: Arc<Bus>
}

impl Capture {
    pub fn run(&mut self, device_name: &str, constellation: &str) {
        info!("Starting GNSS capture for [{}] on [{}]", constellation, device_name);

        let constellation = match GNSSConstellation::try_from(constellation) {
            Ok(constellation) => constellation,
            Err(e) => {
                error!("Unknown GNSS constellation [{}]", constellation);
                return;
            }
        };

        let port = match serialport::new(device_name, 9600)
            .timeout(Duration::from_secs(10))
            .open() {
            Ok(port) => port,
            Err(e) => {
                error!("Failed to open serial port for GNSS device at [{}]: {}", device_name, e);
                return;
            }
        };

        let mut reader = BufReader::new(port);

        let mut line = String::new();
        loop {
            line.clear();
            match reader.read_line(&mut line) {
                Ok(n) if n > 0 => {
                    let timestamp = Utc::now();

                    let nmea = line.trim_end();

                    if nmea.len() < 7 {
                        debug!("Skipping too short NMEA sentence from [{}]: [{}]",
                            &device_name, nmea);
                        continue;
                    }

                    debug!("NMEA sentence from [{}]: [{}]", &device_name, nmea);

                    let message = NMEAMessage {
                        interface: device_name.to_string(),
                        timestamp,
                        sentence: nmea.to_string(),
                        constellation: constellation.clone()
                    };

                    let message_len = (nmea.len() + device_name.len()) as u32;

                    to_pipeline!(
                        GenericChannelName::GnssNmeaMessagesPipeline,
                        self.bus.gnss_nmea_pipeline.sender,
                        Arc::new(message),
                        message_len
                    );

                    match self.metrics.lock() {
                        Ok(mut metrics) => {
                            metrics.increment_processed_bytes_total(message_len);
                            metrics.update_capture(device_name, true, 0, 0);
                        },
                        Err(e) => error!("Could not acquire metrics mutex: {}", e)
                    }
                }
                Ok(_) => {} // Empty line or EOF.
                Err(e) => {
                    error!("Failed to read from serial port for GNSS device at [{}]: {}",
                        device_name, e);
                    return;
                }
            }
        }
    }

    pub fn build_full_capture_name(device_name: &str) -> String {
        let mut device_path = String::from("/dev/");
        device_path.push_str(device_name);

        device_path
    }
}