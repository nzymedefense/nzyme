use std::sync::{Arc, Mutex};
use std::thread;
use log::{debug, error, info};
use rusb::{
    Context, DeviceHandle, Direction, Recipient, RequestType, TransferType, UsbContext,
};
use std::time::{Duration, Instant};
use chrono::{DateTime, Utc};
use systemstat::{Platform, System};
use crate::android::debug::debug_frame::{DebugCaptureInformation, DebugFrame, DebugLogMessage};
use crate::android::vid::is_android_vid;
use crate::configuration::Configuration;
use crate::link::payloads::ConfigurationReport;
use crate::log_buffer::LogBuffer;
use crate::metrics::Metrics;
use crate::rpi::rpi_model::detect_pi_model;
use crate::rpi::rpi_temperature;

// AOA (Android Open Accessory protocol) control request codes.
const AOA_GET_PROTOCOL: u8 = 51;
const AOA_SEND_STRING: u8 = 52;
const AOA_START: u8 = 53;

// Accessory mode IDs. These are the same on Android phones from any manufacturer. It's from the OS.
const AOA_VID: u16 = 0x18d1;
const AOA_PIDS: &[u16] = &[0x2d00, 0x2d01, 0x2d02, 0x2d03, 0x2d04, 0x2d05];

// Accessory identity strings.
const ACCESSORY_MANUFACTURER: &str = "Nzyme Precision Industries, Inc";
const ACCESSORY_MODEL: &str = "NzymeConnect";
const ACCESSORY_DESCRIPTION: &str = "Nzyme Field Debugger";
const ACCESSORY_VERSION: &str = "1.0";
const ACCESSORY_URI: &str = "https://www.nzyme.org/";
const ACCESSORY_SERIAL: &str = "0000000001";

// Timeouts.
const POLL_INTERVAL: Duration = Duration::from_secs(2);
const PROBE_TIMEOUT: Duration = Duration::from_millis(500);
const HANDSHAKE_TIMEOUT: Duration = Duration::from_secs(1);
const RE_ENUMERATE_TIMEOUT: Duration = Duration::from_secs(5);
const BULK_TIMEOUT: Duration = Duration::from_secs(3);
const LIVENESS_TIMEOUT: Duration = Duration::from_millis(500);
const TICK_INTERVAL: Duration = Duration::from_secs(1);
const ERROR_BACKOFF: Duration = Duration::from_secs(5);

enum SessionOutcome {
    /// No phone present on the bus.
    Idle,

    /// A session ended. device unplugged or app stopped mid-stream
    /// after we had successfully been streaming.
    Ended,

    /// Phone present and in accessory mode, but nothing is listening on the
    /// other end. (app closed while USB stayed connected)
    DeadConnection,

    /// We think it's a phone, but the AOA handshake didn't complete.
    HandshakeFailed,
}

pub struct AndroidDebugListener {
    process_started_at: DateTime<Utc>,
    configuration: Configuration,
    metrics: Arc<Mutex<Metrics>>,
    log_buffer: Arc<LogBuffer>,
    system: System,
}

impl AndroidDebugListener {
    pub fn new(process_started_at: DateTime<Utc>,
               configuration: Configuration,
               metrics: Arc<Mutex<Metrics>>,
               log_buffer: Arc<LogBuffer>) -> Self {
        Self {
            process_started_at,
            configuration,
            metrics,
            log_buffer,
            system: System::new(),
        }
    }

    pub fn listen(&self) -> ! {
        loop {
            let _ = std::panic::catch_unwind(std::panic::AssertUnwindSafe(|| {
                self.run();
            }));
            std::thread::sleep(ERROR_BACKOFF);
        }
    }

    fn run(&self) {
        let ctx = match Context::new() {
            Ok(c) => c,
            Err(_) => return,
        };

        loop {
            match self.try_session(&ctx) {
                SessionOutcome::Idle => thread::sleep(POLL_INTERVAL),
                SessionOutcome::Ended => thread::sleep(POLL_INTERVAL),
                SessionOutcome::DeadConnection => {
                    /*
                     * Phone is plugged in and in accessory mode, but no app is reading.
                     * This is a normal/expected state and we back off silently.
                     */
                    thread::sleep(ERROR_BACKOFF);
                }
                SessionOutcome::HandshakeFailed => {
                    debug!("AOA handshake did not complete.");
                    thread::sleep(ERROR_BACKOFF);
                }
            }
        }
    }

    fn try_session(&self, ctx: &Context) -> SessionOutcome {
        if let Some(handle) = find_accessory(ctx) {
            return self.run_session(handle);
        }

        let phone = match find_handshake_candidate(ctx) {
            Some(h) => h,
            None => return SessionOutcome::Idle,
        };

        if !send_handshake(&phone) {
            return SessionOutcome::HandshakeFailed;
        }
        drop(phone);

        match wait_for_accessory(ctx, RE_ENUMERATE_TIMEOUT) {
            Some(h) => self.run_session(h),
            None => SessionOutcome::HandshakeFailed,
        }
    }

    fn run_session(&self, handle: DeviceHandle<Context>) -> SessionOutcome {
        let endpoints = match find_bulk_endpoints(&handle) {
            Some(e) => e,
            None => return SessionOutcome::HandshakeFailed,
        };

        handle.set_auto_detach_kernel_driver(true).ok();
        if handle.claim_interface(endpoints.interface).is_err() {
            return SessionOutcome::HandshakeFailed;
        }

        let mut streaming = false;

        let outcome = loop {

            if handle.write_bulk(endpoints.bulk_out, &[], LIVENESS_TIMEOUT).is_err() {
                break if streaming {
                    SessionOutcome::Ended
                } else {
                    SessionOutcome::DeadConnection
                };
            }

            let frame = match self.build_frame() {
                Some(f) => f,
                None => {
                    thread::sleep(TICK_INTERVAL);
                    continue;
                }
            };

            let json = match serde_json::to_string(&frame) {
                Ok(j) => j,
                Err(e) => {
                    error!("Failed to serialize Android debug frame: {}", e);
                    thread::sleep(TICK_INTERVAL);
                    continue;
                }
            };

            let payload = format!("{json}\n");
            match handle.write_bulk(endpoints.bulk_out, payload.as_bytes(), BULK_TIMEOUT) {
                Ok(_) => {
                    if !streaming {
                        streaming = true;
                        info!("Android debug session started.");
                    }
                }
                Err(_) => {
                    break if streaming {
                        info!("Android debug session ended.");
                        SessionOutcome::Ended
                    } else {
                        SessionOutcome::DeadConnection
                    };
                }
            }

            std::thread::sleep(TICK_INTERVAL);
        };

        handle.release_interface(endpoints.interface).ok();
        outcome
    }

    fn build_frame(&self) -> Option<DebugFrame> {
        let (processed_bytes_total, processed_bytes_avg, captures) = match self.metrics.lock() {
            Ok(metrics) => {
                let mut captures: Vec<DebugCaptureInformation> = Vec::new();
                for capture in metrics.get_captures().values() {
                    captures.push(DebugCaptureInformation {
                        capture_type: capture.capture_type.to_string(),
                        interface_name: capture.interface_name.clone(),
                        is_running: capture.is_running,
                        received: capture.received,
                        dropped_buffer: capture.dropped_buffer.avg,
                        dropped_interface: capture.dropped_interface.avg,
                    });
                }

                (metrics.get_processed_bytes().total, metrics.get_processed_bytes().avg, captures)
            }
            Err(e) => {
                error!("Could not acquire metrics lock: {}", e);
                return None;
            }
        };

        let cpu_load = self.sample_cpu_load();
        let (memory_total, memory_free) = self.sample_memory();

        let mut logs: Vec<DebugLogMessage> = Vec::new();
        for log in self.log_buffer.snapshot() {
            logs.push(DebugLogMessage {
                timestamp: log.timestamp,
                level: log.level.to_string(),
                message: log.message.clone(),
            });
        }

        let configuration = match ConfigurationReport::try_from(self.configuration.clone()) {
            Ok(c) => c,
            Err(_) => {
                error!("Could not build configuration report for debug frame.");
                return None;
            }
        };

        Some(DebugFrame {
            process_started_at: self.process_started_at,
            leader_uri: self.configuration.general.leader_uri.clone(),
            version: env!("CARGO_PKG_VERSION").to_string(),
            rpi_temperature: detect_pi_model().map(|_| rpi_temperature::read_cpu_temp_c()),
            memory_total,
            memory_free,
            cpu_load,
            processed_bytes_total,
            processed_bytes_avg,
            configuration,
            captures,
            logs,
        })
    }

    fn sample_cpu_load(&self) -> f32 {
        match self.system.cpu_load_aggregate() {
            Ok(cpu) => {
                // Brief sample window to let the aggregate gather data.
                thread::sleep(Duration::from_secs(1));
                match cpu.done() {
                    Ok(cpu) => (cpu.user + cpu.nice + cpu.system + cpu.interrupt) * 100.0,
                    Err(e) => {
                        error!("Could not determine CPU load average. {}", e);
                        0.0
                    }
                }
            }
            Err(e) => {
                error!("Could not determine CPU load average. {}", e);
                0.0
            }
        }
    }

    fn sample_memory(&self) -> (u64, u64) {
        match self.system.memory() {
            Ok(mem) => (mem.total.as_u64(), mem.free.as_u64()),
            Err(e) => {
                error!("Could not determine memory metrics. {}", e);
                (0, 0)
            }
        }
    }
}

fn find_accessory(ctx: &Context) -> Option<DeviceHandle<Context>> {
    let devices = ctx.devices().ok()?;
    for device in devices.iter() {
        let desc = match device.device_descriptor() {
            Ok(d) => d,
            Err(_) => continue,
        };
        if desc.vendor_id() == AOA_VID && AOA_PIDS.contains(&desc.product_id()) {
            if let Ok(h) = device.open() {
                return Some(h);
            }
        }
    }
    None
}

fn find_handshake_candidate(ctx: &Context) -> Option<DeviceHandle<Context>> {
    let devices = ctx.devices().ok()?;
    for device in devices.iter() {
        let desc = match device.device_descriptor() {
            Ok(d) => d,
            Err(_) => continue,
        };
        let vid = desc.vendor_id();
        let pid = desc.product_id();

        if !is_android_vid(vid) {
            continue;
        }

        if vid == AOA_VID && AOA_PIDS.contains(&pid) {
            continue;
        }

        let handle = match device.open() {
            Ok(h) => h,
            Err(_) => continue,
        };

        let req_type = rusb::request_type(Direction::In, RequestType::Vendor, Recipient::Device);
        let mut buf = [0u8; 2];
        let proto = match handle.read_control(
            req_type, AOA_GET_PROTOCOL, 0, 0, &mut buf, PROBE_TIMEOUT,
        ) {
            Ok(_) => u16::from_le_bytes(buf),
            Err(_) => continue, // Device doesn't speak AOA.
        };
        if proto < 1 {
            continue;
        }
        debug!("AOA-capable device {:04x}:{:04x}, protocol v{}.", vid, pid, proto);
        return Some(handle);
    }
    None
}

fn send_handshake(handle: &DeviceHandle<Context>) -> bool {
    let strings = [
        (0u16, ACCESSORY_MANUFACTURER),
        (1, ACCESSORY_MODEL),
        (2, ACCESSORY_DESCRIPTION),
        (3, ACCESSORY_VERSION),
        (4, ACCESSORY_URI),
        (5, ACCESSORY_SERIAL),
    ];

    let req_type = rusb::request_type(Direction::Out, RequestType::Vendor, Recipient::Device);

    for (idx, s) in strings {
        if handle.write_control(req_type, AOA_SEND_STRING, 0, idx, s.as_bytes(), HANDSHAKE_TIMEOUT).is_err() {
            return false;
        }
    }

    handle.write_control(req_type, AOA_START, 0, 0, &[], HANDSHAKE_TIMEOUT).is_ok()
}

fn wait_for_accessory(ctx: &Context, timeout: Duration) -> Option<DeviceHandle<Context>> {
    let deadline = Instant::now() + timeout;
    while Instant::now() < deadline {
        if let Some(h) = find_accessory(ctx) {
            return Some(h);
        }
        thread::sleep(Duration::from_millis(200));
    }
    None
}

struct Endpoints {
    interface: u8,
    bulk_out: u8,
}

fn find_bulk_endpoints(handle: &DeviceHandle<Context>) -> Option<Endpoints> {
    let device = handle.device();
    let config = device.active_config_descriptor().ok()?;
    for interface in config.interfaces() {
        for desc in interface.descriptors() {
            let mut bulk_out = None;
            for ep in desc.endpoint_descriptors() {
                if ep.transfer_type() != TransferType::Bulk {
                    continue;
                }
                if ep.direction() == Direction::Out {
                    bulk_out = Some(ep.address());
                }
            }
            if let Some(out) = bulk_out {
                return Some(Endpoints {
                    interface: desc.interface_number(),
                    bulk_out: out,
                });
            }
        }
    }
    None
}