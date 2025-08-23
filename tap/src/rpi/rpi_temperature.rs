use std::fs;
use log::error;

const TEMPERATURE_PATH: &str = "/sys/class/thermal/thermal_zone0/temp";

pub fn read_cpu_temp_c() -> f32 {
    let raw = match fs::read_to_string(TEMPERATURE_PATH) {
        Ok(x) => x,
        Err(e) => {
            error!("Could not read RPI temperature: {}", e);
            return 0.0
        }
    };

    let milli_c: i64 = match raw.trim().parse() {
        Ok(x) => x,
        Err(e) => {
            error!("Could not parse RPI temperature: {}", e);
            return 0.0
        }
    };

    milli_c as f32 / 1000.0
}