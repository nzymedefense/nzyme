use std::io::Read;
use std::sync::{Arc, Mutex};
use std::thread::sleep;
use std::time::Duration;
use chrono::{DateTime, Utc};
use log::{debug, error, info, warn};
use crate::configuration::GNSSInterface;
use crate::messagebus::bus::Bus;
use crate::messagebus::channel_names::GenericChannelName;
use crate::metrics::Metrics;
use crate::to_pipeline;
use crate::usb::usb::{find_first_nzyme_usb_device_with_pid_and_serial};
use crate::wireless::positioning::axia::axia::AXIA_1_PID;
use crate::wireless::positioning::axia::sentence_type::SentenceType;
use crate::wireless::positioning::axia::ubx::{AntennaPower, AntennaStatus, JammingState, UbxMonRfMessage, UbxMonRfBlock, UbxRxmMeasxMessage, UbxRxmMeasxSat};
use crate::wireless::positioning::gnss_constellation::GNSSConstellation;
use crate::wireless::positioning::nmea::nmea_message::NMEAMessage;

const FRAME_MAGIC0: u8 = b'n';
const FRAME_MAGIC1: u8 = b'z';

const HEADER_LEN: usize = 6;
const MAX_PAYLOAD_LEN: usize = 4096;

#[derive(Debug)]
pub struct DataFrame {
    pub constellation: GNSSConstellation,
    pub sentence_type: SentenceType,
    pub payload: Vec<u8>,
    pub timestamp: DateTime<Utc>
}

pub struct Capture {
    pub metrics: Arc<Mutex<Metrics>>,
    pub bus: Arc<Bus>
}

impl Capture {

    pub fn run(&mut self, device_config: &GNSSInterface, serial: &str) {
        info!("Attempting to detect Axia [{}].", serial);
        let device = match find_first_nzyme_usb_device_with_pid_and_serial(AXIA_1_PID, serial) {
            Ok(device) => {
                match device {
                    Some(device) => device,
                    None => {
                        warn!("No Axia device discovered. Expected PID [0x{:04X}].", AXIA_1_PID);
                        return;
                    }
                }
            },
            Err(e) => {
                error!("Could not detect USB devices: {}", e);
                return;
            }
        };

        info!("Axia detected: [{:?}]", device);

        let acm_port = match device.acm_port {
            Some(acm_port) => acm_port,
            None => {
                error!("Axia at {}:{} does not expose a ACM port.", device.bus, device.address);
                return;
            }
        };

        info!("Starting Axia GNSS capture on [{}]", acm_port);

        let mut port = match serialport::new(&acm_port, 115_200)
            .timeout(Duration::from_millis(1000))
            .open() {
            Ok(p) => p,
            Err(e) => {
                error!("Failed to open serial port for Axia at [{}]: {}", acm_port, e);
                return
            }
        };

        let mut read_buf = [0u8; 2048];
        let mut buffer: Vec<u8> = Vec::with_capacity(4096);

        loop {
            match port.read(&mut read_buf) {
                Ok(n) if n > 0 => {
                    buffer.extend_from_slice(&read_buf[..n]);
                    self.process_buffer(device_config, &mut buffer, serial);
                }
                Ok(_) => {
                    // Nothing read.
                }
                Err(e) => {
                    // We ignore time out errors that happen sometimes.
                    if e.kind() != std::io::ErrorKind::TimedOut {
                        // Exit on any other error. Likely a disconnected device.
                        error!("Serial read error: {:?}", e);
                        sleep(Duration::from_millis(1000));
                        return;
                    }
                }
            }
        }
    }

    fn process_buffer(&self, device_config: &GNSSInterface, buf: &mut Vec<u8>, serial: &str) {
        loop {
            if buf.len() < HEADER_LEN {
                return;
            }

            // Find magic bytes "nz".
            let mut start_idx = None;
            for i in 0..(buf.len() - 1) {
                if buf[i] == FRAME_MAGIC0 && buf[i + 1] == FRAME_MAGIC1 {
                    start_idx = Some(i);
                    break;
                }
            }

            match start_idx {
                None => {
                    buf.clear();
                    return;
                }
                Some(0) => {} // already aligned
                Some(idx) => {
                    buf.drain(0..idx);
                    if buf.len() < HEADER_LEN {
                        return;
                    }
                }
            }

            let constellation_raw = buf[2];
            let sentence_type_raw = buf[3];
            let payload_len = u16::from_le_bytes([buf[4], buf[5]]) as usize;

            // Sanity check payload length.
            if payload_len == 0 || payload_len > MAX_PAYLOAD_LEN {
                warn!("Invalid payload length {} – dropping frame & resyncing", payload_len);
                buf.drain(0..2); // drop the "nz" and resync
                continue;
            }

            let frame_len = HEADER_LEN + payload_len;

            if buf.len() < frame_len {
                return; // not enough bytes yet
            }

            let sentence_type = SentenceType::from_u8(sentence_type_raw);
            let payload_slice = &buf[HEADER_LEN..frame_len];

            // ASCII validation for NMEA.
            if matches!(sentence_type, SentenceType::Nmea) {
                if !payload_slice.iter().all(|b| b.is_ascii()) {
                    log::debug!("Dropping corrupted NMEA frame: {:?}", payload_slice);
                    buf.drain(0..frame_len);
                    continue;
                }
            }

            let payload = payload_slice.to_vec();

            let frame = DataFrame {
                constellation: GNSSConstellation::from_axia_u8(constellation_raw),
                sentence_type,
                payload,
                timestamp: Utc::now(),
            };

            self.handle_frame(device_config, &frame, serial);

            buf.drain(0..frame_len);
        }
    }

    fn handle_frame(&self, device_config: &GNSSInterface, frame: &DataFrame, serial: &str) {
        match frame.sentence_type {
            SentenceType::Nmea => self.handle_nmea_frame(device_config, frame, serial),
            SentenceType::UbxMonRf => self.handle_ubx_mon_rf_frame(frame),
            SentenceType::UbxRxmMeasx => self.handle_ubx_rxm_measx_frame(frame),
            SentenceType::Unknown(t) => warn!("Unknown Axia sentence type {}", t)
        }
    }

    fn handle_nmea_frame(&self, device_config: &GNSSInterface, frame: &DataFrame, serial: &str) {
        // Payload is just NMEA at this point. We do need to remove trailing newlines though.
        let frame_msg = String::from_utf8_lossy(&frame.payload);
        let nmea = frame_msg.trim_end_matches(&['\r', '\n']);

        debug!("Axia NMEA sentence: {}", nmea);

        let message = NMEAMessage {
            interface: "axia".to_string(),
            timestamp: frame.timestamp,
            sentence: nmea.to_string(),
            offset_lat: device_config.offset_lat,
            offset_lon: device_config.offset_lon
        };

        to_pipeline!(GenericChannelName::GnssNmeaMessagesPipeline,
            self.bus.gnss_nmea_pipeline.sender,
            Arc::new(message),
            nmea.len() as u32
        );

        match self.metrics.lock() {
            Ok(mut metrics) => {
                metrics.increment_processed_bytes_total(nmea.len() as u32);
                metrics.update_capture(&format!("axia-{}", serial), true, 0, 0);
            },
            Err(e) => error!("Could not acquire metrics mutex: {}", e)
        }
    }

    fn handle_ubx_mon_rf_frame(&self, frame: &DataFrame) {
        match self.parse_ubx_mon_rf(&frame.payload, &frame.constellation) {
            Some(mon_rf) => {
                if mon_rf.blocks.len() != 1 {
                    warn!("GNSS UBX MON-RF message reports unexpected number of blocks: [{:?}]",
                        mon_rf);
                    return;
                }

                let size = mon_rf.estimate_size() as u32;
                to_pipeline!(GenericChannelName::GnssUbxMonRfPipeline,
                    self.bus.gnss_ubx_mon_rf_pipeline.sender,
                    Arc::new(mon_rf),
                    size
                );
            }
            None => {
                error!("Could not parse Axia MON-RF payload");
            }
        }
    }

    fn handle_ubx_rxm_measx_frame(&self, frame: &DataFrame) {
        match self.parse_ubx_rxm_measx(&frame.payload, &frame.constellation) {
            Some(measx) => {
                let size = measx.estimate_size() as u32;
                to_pipeline!(GenericChannelName::GnssUbxRxmMeasxPipeline,
                    self.bus.gnss_ubx_rxm_measx_pipeline.sender,
                    Arc::new(measx),
                    size
                );
            }
            None => {
                error!("Could not parse Axia RXM-MEASX payload");
            }
        }
    }

    fn parse_ubx_mon_rf(&self, ubx: &[u8], constellation: &GNSSConstellation) -> Option<UbxMonRfMessage> {
        if ubx.len() < 8 {
            error!("UBX buffer too short for header: len={}", ubx.len());
            return None;
        }

        if ubx[0] != 0xB5 || ubx[1] != 0x62 {
            error!("UBX sync mismatch: {:02X} {:02X}", ubx[0], ubx[1]);
            return None;
        }

        let msg_class = ubx[2];
        let msg_id = ubx[3];

        if msg_class != 0x0A || msg_id != 0x38 {
            error!("Unexpected UBX message class/id: class=0x{:02X} id=0x{:02X}", msg_class, msg_id);
            return None;
        }

        let payload_len = u16::from_le_bytes([ubx[4], ubx[5]]) as usize;
        if ubx.len() < 6 + payload_len + 2 {
            error!("UBX buffer too short for declared payload: len={} payload_len={}",
            ubx.len(), payload_len);

            return None;
        }

        let payload = &ubx[6..6 + payload_len];

        if payload_len < 4 {
            error!("UBX-MON-RF payload too short: {}", payload_len);
            return None;
        }

        let version = payload[0];
        let n_blocks = payload[1];
        let _reserved1 = &payload[2..4];

        let expected_len = 4usize + (n_blocks as usize) * 24usize;
        if payload_len != expected_len {
            error!("UBX-MON-RF length mismatch: payload_len={} expected={}", payload_len, expected_len);
            return None;
        }

        let mut blocks = Vec::new();
        for i in 0..(n_blocks as usize) {
            let base = 4 + i * 24;
            if base + 24 > payload.len() {
                error!( "UBX-MON-RF truncated at block {}: base={} payload_len={}",
                i, base, payload.len());
                break;
            }

            let block_id = payload[base + 0];
            let flags = payload[base + 1];
            let ant_status_raw = payload[base + 2];
            let ant_power_raw = payload[base + 3];

            let post_status = u32::from_le_bytes([
                payload[base + 4],
                payload[base + 5],
                payload[base + 6],
                payload[base + 7],
            ]);

            let noise_per_ms = u16::from_le_bytes([payload[base + 12], payload[base + 13]]);
            let agc_cnt = u16::from_le_bytes([payload[base + 14], payload[base + 15]]);
            let jam_ind = payload[base + 16];
            let ofs_i = payload[base + 17] as i8;
            let mag_i = payload[base + 18];
            let ofs_q = payload[base + 19] as i8;
            let mag_q = payload[base + 20];

            let block = UbxMonRfBlock {
                block_id,
                flags,
                jamming_state: JammingState::from_flags(flags),
                antenna_status: AntennaStatus::from_u8(ant_status_raw),
                antenna_power: AntennaPower::from_u8(ant_power_raw),
                post_status,
                noise_per_ms,
                agc_cnt,
                jam_ind,
                ofs_i,
                mag_i,
                ofs_q,
                mag_q,
            };

            blocks.push(block);
        }

        Some(UbxMonRfMessage { version, blocks, constellation: constellation.clone() })
    }

    fn parse_ubx_rxm_measx(&self, ubx: &[u8], constellation: &GNSSConstellation)
            -> Option<UbxRxmMeasxMessage> {

        // UBX header sanity checks.
        if ubx.len() < 8 {
            error!("UBX buffer too short for header: len={}", ubx.len());
            return None;
        }
        if ubx[0] != 0xB5 || ubx[1] != 0x62 {
            error!("UBX sync mismatch: {:02X} {:02X}", ubx[0], ubx[1]);
            return None;
        }

        let msg_class = ubx[2];
        let msg_id = ubx[3];
        if msg_class != 0x02 || msg_id != 0x14 {
            error!("Unexpected UBX message class/id for MEASX: class=0x{:02X} id=0x{:02X}",
                msg_class, msg_id);
            return None;
        }

        let payload_len = u16::from_le_bytes([ubx[4], ubx[5]]) as usize;
        if ubx.len() < 6 + payload_len + 2 {
            error!("UBX buffer too short for declared payload: len={} payload_len={}",
                ubx.len(), payload_len);
            return None;
        }

        let payload = &ubx[6..6 + payload_len];

        // Per NEO-M9N interface description: payload is 44 + numSV*24.
        if payload_len < 44 {
            error!("UBX-RXM-MEASX payload too short: {}", payload_len);
            return None;
        }

        let version = payload[0];

        let gps_tow_ms = u32::from_le_bytes([payload[4], payload[5], payload[6], payload[7]]);
        let glo_tow_ms = u32::from_le_bytes([payload[8], payload[9], payload[10], payload[11]]);
        let bds_tow_ms = u32::from_le_bytes([payload[12], payload[13], payload[14], payload[15]]);
        let qzss_tow_ms = u32::from_le_bytes([payload[20], payload[21], payload[22], payload[23]]);
        let gps_tow_acc = u16::from_le_bytes([payload[24], payload[25]]);
        let glo_tow_acc = u16::from_le_bytes([payload[26], payload[27]]);
        let bds_tow_acc = u16::from_le_bytes([payload[28], payload[29]]);
        let qzss_tow_acc = u16::from_le_bytes([payload[32], payload[33]]);

        let num_sv = payload[34];
        let flags = payload[35];
        let tow_set = flags & 0x03;


        let expected_len = 44usize + (num_sv as usize) * 24usize;
        if payload_len != expected_len {
            error!("UBX-RXM-MEASX length mismatch: payload_len={} expected={}",
                payload_len, expected_len);
            return None;
        }

        let mut sats = Vec::with_capacity(num_sv as usize);

        for n in 0..(num_sv as usize) {
            let base = 44 + n * 24;

            let gnss_id = payload[base + 0];
            let sv_id = payload[base + 1];
            let sno = payload[base + 2];
            let mpath_indic = payload[base + 3];

            let doppler_ms = i32::from_le_bytes([
                payload[base + 4],
                payload[base + 5],
                payload[base + 6],
                payload[base + 7],
            ]);

            let doppler_hz = i32::from_le_bytes([
                payload[base + 8],
                payload[base + 9],
                payload[base + 10],
                payload[base + 11],
            ]);

            let whole_chips = u16::from_le_bytes([payload[base + 12], payload[base + 13]]);
            let frac_chips = u16::from_le_bytes([payload[base + 14], payload[base + 15]]);

            let code_phase = u32::from_le_bytes([
                payload[base + 16],
                payload[base + 17],
                payload[base + 18],
                payload[base + 19],
            ]);

            let int_code_phase = payload[base + 20];
            let pseurange_rms_err = payload[base + 21];

            sats.push(UbxRxmMeasxSat {
                gnss_id,
                sv_id,
                sno,
                mpath_indic,
                doppler_ms,
                doppler_hz,
                whole_chips,
                frac_chips,
                code_phase,
                int_code_phase,
                pseurange_rms_err,
            });
        }

        Some(UbxRxmMeasxMessage {
            version,
            gps_tow_ms,
            glo_tow_ms,
            bds_tow_ms,
            qzss_tow_ms,
            gps_tow_acc,
            glo_tow_acc,
            bds_tow_acc,
            qzss_tow_acc,
            num_sv,
            flags,
            tow_set,
            sats,
            constellation: constellation.clone(),
        })
    }

}