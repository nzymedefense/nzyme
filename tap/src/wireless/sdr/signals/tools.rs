
pub fn linear_power_to_dbm(power_mw: f64) -> f64 {
    10.0 * power_mw.log10()
}

pub fn dbm_to_linear_power(dbm: f64) -> f64 {
    10.0f64.powf(dbm / 10.0)
}

pub fn format_frequency(frequency_hz: f64) -> String {
    if frequency_hz >= 1_000_000_000.0 {
        // GHz
        format!("{:.3} GHz", frequency_hz / 1_000_000_000.0)
    } else if frequency_hz >= 1_000_000.0 {
        // MHz
        format!("{:.3} MHz", frequency_hz / 1_000_000.0)
    } else if frequency_hz >= 1_000.0 {
        // kHz
        format!("{:.3} kHz", frequency_hz / 1_000.0)
    } else {
        // Hz
        format!("{:.0} Hz", frequency_hz)
    }
}