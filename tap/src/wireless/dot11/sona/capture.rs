use std::sync::{Arc, Mutex};
use std::time::{Duration};
use crossbeam_channel::Receiver;
use log::{debug, error, info, trace, warn};
use serialport::ClearBuffer;
use crate::messagebus::bus::Bus;
use crate::metrics::Metrics;
use crate::peripherals::cobs::{cobs_decode};
use crate::usb::usb::find_first_nzyme_usb_device_with_pid_and_serial;
use crate::wireless::dot11::sona::commands::SonaCommand;
use crate::wireless::dot11::sona::protocol::{process_decoded, send_set_frequency};
use crate::wireless::dot11::sona::sona::SONA_1_PID;
use crate::wireless::dot11::sona::uptime_offset::UptimeOffset;

const MAX_ACCUMULATED_BYTES_WITHOUT_DELIMITER: usize = 8192;

pub struct Capture {
    pub metrics: Arc<Mutex<Metrics>>,
    pub bus: Arc<Bus>,
    pub command_receiver: Receiver<SonaCommand>,
}

impl Capture {
    pub fn run(&self, interface_name: &str, serial: &str) {
        info!("Initializing Sona with serial [{}].", serial);

        let sona = match find_first_nzyme_usb_device_with_pid_and_serial(SONA_1_PID, serial) {
            Ok(Some(sona)) => sona,
            Ok(None) => {
                error!("No Sona with serial [{}] found.", serial);
                return;
            }
            Err(e) => {
                error!("Could not search for Sona with serial [{}]: {}", serial, e);
                return;
            }
        };

        let acm_port = match sona.acm_port {
            Some(acm_port) => acm_port,
            None => {
                error!("Found Sona with serial [{}] at [{}:{}] but it is not exposing an ACM port.",
                    serial, sona.bus, sona.address);
                return;
            }
        };

        info!("Found Sona with serial [{}] at [{}:{}]: {}", serial, sona.bus, sona.address, acm_port);

        let mut port_handle = match serialport::new(acm_port.clone(), 115_200)
            .timeout(Duration::from_millis(50))
            .open() {
            Ok(mut port) => {
                if let Err(e) = port.write_data_terminal_ready(true) {
                    error!("Failed to set DTR on ACM port [{}]: {}", acm_port, e);
                    return;
                }
                debug!("DTR set on Sona [{}]", serial);
                port
            }
            Err(e) => {
                error!("Could not open ACM port [{}] of Sona with serial [{}] at [{}:{}]: {}",
                    acm_port, serial, sona.bus, sona.address, e);
                return;
            }
        };

        /*
         * Sync to the stream.
         *
         * The firmware streams continuously regardless of whether anyone is
         * reading. When we open the port, the OS may have buffered partial
         * data from before this process existed, and the firmware itself is
         * very likely mid-frame on the wire. We sleep briefly so any
         * in-flight bytes settle, then drop everything in the input buffer.
         * The next 0x00 we read after this point is, by COBS construction,
         * a real frame boundary — and even if the very next decode after
         * that fails (we lost a partial frame), the one after will succeed.
         */
        std::thread::sleep(Duration::from_millis(150));
        if let Err(e) = port_handle.clear(ClearBuffer::Input) {
            warn!("Could not flush input buffer on Sona [{}]: {}", serial, e);
        }

        info!("Connected to Sona [{}] ACM port. Starting data capture.", serial);

        let mut chunk = [0u8; 512];
        let mut acc: Vec<u8> = Vec::with_capacity(MAX_ACCUMULATED_BYTES_WITHOUT_DELIMITER);
        let mut uptime_offset: Option<UptimeOffset> = None;

        loop {
            // Drain any pending host commands.
            while let Ok(cmd) = self.command_receiver.try_recv() {
                match cmd {
                    SonaCommand::SetFrequency(freq_mhz) => {
                        if let Err(e) = send_set_frequency(&mut *port_handle, freq_mhz) {
                            warn!("Failed to send SetFrequency({}) to Sona [{}]: {}",
                                freq_mhz, serial, e);
                        } else {
                            debug!("Sent SetFrequency({}) to Sona [{}].", freq_mhz, serial);
                        }
                    }
                }
            }

            match port_handle.read(&mut chunk) {
                Ok(n) if n > 0 => {
                    if acc.len() + n > MAX_ACCUMULATED_BYTES_WITHOUT_DELIMITER {
                        warn!("Accumulator exceeded maximum bytes without delimiter: <{}> bytes, \
                            clearing buffer.", MAX_ACCUMULATED_BYTES_WITHOUT_DELIMITER);
                        acc.clear();
                        continue;
                    }

                    acc.extend_from_slice(&chunk[..n]);

                    let mut processed_up_to = 0;
                    while let Some(delimiter_pos) = acc[processed_up_to..].iter().position(|&b| b == 0) {
                        let frame_end = processed_up_to + delimiter_pos;

                        if frame_end > processed_up_to {
                            let encoded = &acc[processed_up_to..frame_end];

                            match cobs_decode(encoded) {
                                Ok(decoded) => {
                                    process_decoded(
                                        interface_name,
                                        &decoded,
                                        &mut uptime_offset,
                                        self.metrics.clone(),
                                        self.bus.clone()
                                    );
                                }
                                Err(e) => {
                                    /*
                                     * Expected once per session at startup
                                     * (we land mid-frame after the flush),
                                     * and on any rare wire corruption. Trace,
                                     * not warn.
                                     */
                                    trace!("COBS decode error: {}", e);
                                }
                            }
                        }

                        processed_up_to = frame_end + 1;
                    }

                    if processed_up_to > 0 {
                        acc.drain(0..processed_up_to);
                    }
                }
                Ok(_) => { /* Zero bytes. Loop. */ }
                Err(ref e) if e.kind() == std::io::ErrorKind::TimedOut => { /* normal */ }
                Err(e) => {
                    error!("Sona [{}] ACM port [{}] read error: {}", serial, acm_port, e);
                    return;
                }
            }
        }
    }
}