use core::panic;
use std::backtrace::Backtrace;
use std::fs::OpenOptions;
use std::sync::Arc;
use fern::colors::{ColoredLevelConfig, Color};
use log::{info, LevelFilter, warn};
use crate::log_monitor::LogMonitor;
use std::io::Write;
use crate::log_buffer::{LogBuffer, LogBufferLine};

pub fn initialize(config_level: &str,
                  log_monitor: &Arc<LogMonitor>,
                  log_buffer: &Arc<LogBuffer>) {
    let mut unknown_filter = false;
    let filter = match config_level.to_uppercase().as_str() {
        "OFF" => LevelFilter::Off,
        "ERROR" => LevelFilter::Error,
        "WARN" => LevelFilter::Warn,
        "INFO" => LevelFilter::Info,
        "DEBUG" => LevelFilter::Debug,
        "TRACE" => LevelFilter::Trace,
        _ => {
            // We can't log here yet, so saving for a warning later.
            unknown_filter = true;
            LevelFilter::Info
        }
    };

    let colors_line = ColoredLevelConfig::new()
        .error(Color::Red)
        .warn(Color::Yellow)
        .info(Color::BrightWhite)
        .debug(Color::Blue)
        .trace(Color::BrightBlack);

    let monitor = log_monitor.clone();
    let buffer = log_buffer.clone();
    let result = fern::Dispatch::new()
        .format(move |out, message, record| {
            let now = chrono::Local::now();
            monitor.mark(&record.level());

            buffer.push(LogBufferLine {
                timestamp: now,
                level: record.level(),
                message: message.to_string(),
            });

            out.finish(format_args!(
                "{color_line}[{level}][{date}][{target}]{color_line} {message}\x1B[0m",
                color_line = format_args!(
                    "\x1B[{}m",
                    colors_line.get_color(&record.level()).to_fg_str()
                ),
                date = now.format("%Y-%m-%d %H:%M:%S"),
                target = format_target(record.target()),
                level = record.level(),
                message = message,
            ));
        })
        .level(filter)
        .chain(std::io::stdout())
        .apply();

    match result {
        Ok(_) => info!("Initialized logging with level filter [{}].", filter),
        Err(e) => panic!("Could not initialize logging: {}", e),
    }

    if unknown_filter {
        warn!("Unknown provided logging level filter [{}], fell back to default filter [{}].", 
            config_level.to_uppercase(), filter);
    }
}

fn format_target(target: &str) -> String {
    let mut vec: Vec<&str> = target.split("::").collect();

    if vec.len() <= 1 {
        return target.to_string();
    }

    vec.remove(0);
    vec.join("::")
}

pub fn install_panic_logger(path: String) {
    let default = std::panic::take_hook();
    let path = path.to_string();

    std::panic::set_hook(Box::new(move |info| {
        let bt = Backtrace::force_capture();
        let ts = chrono::Local::now().format("%Y-%m-%d %H:%M:%S%.3f");
        let thread = std::thread::current().name().unwrap_or("<unnamed>").to_owned();

        match OpenOptions::new().create(true).append(true).open(&path) {
            Ok(mut f) => {
                let _ = writeln!(f, "[{ts}][PANIC][thread={thread}] {info}\n{bt}");
                let _ = f.flush();
            }
            Err(e) => {
                eprintln!("PANIC LOGGER: could not open [{}]: {}", path, e);
            }
        }

        default(info);
    }));
}