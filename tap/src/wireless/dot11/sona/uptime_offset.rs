/*
 * Tracks the offset between firmware uptime (ms since firmware boot) and
 * host wall-clock time. Anchored on the first frame of a session and then
 * applied to every subsequent frame so per-frame timestamps don't carry the
 * USB transit jitter that would come from stamping at receive time.
 */
use std::time::{SystemTime, UNIX_EPOCH};

pub struct UptimeOffset {
    /*
     * host_unix_micros - firmware_uptime_micros, captured at the anchor.
     * Signed because firmware uptime starts at ~0 while host wall-clock is
     * in present.
     */
    offset_micros: i64,

    /*
     * Last firmware uptime we observed, used to detect 32-bit ms wraparound
     * (~49.7 days) or a missed reboot so we can re-anchor.
     */
    last_uptime_ms: u32,
}

impl UptimeOffset {
    pub fn new(uptime_ms: u32) -> Self {
        let host_micros = host_unix_micros();
        let firmware_micros = (uptime_ms as i64) * 1000;
        Self {
            offset_micros: host_micros - firmware_micros,
            last_uptime_ms: uptime_ms,
        }
    }

    /*
     * Returns wall-clock unix microseconds for a frame captured at the given
     * firmware uptime. Re-anchors on a big backward jump (wraparound or
     * missed reboot).
     */
    pub fn wall_micros(&mut self, uptime_ms: u32) -> i64 {
        if uptime_ms < self.last_uptime_ms.saturating_sub(60_000) {
            *self = Self::new(uptime_ms);
        }
        self.last_uptime_ms = uptime_ms;
        self.offset_micros + (uptime_ms as i64) * 1000
    }
}

fn host_unix_micros() -> i64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .map(|d| d.as_micros() as i64)
        .unwrap_or(0)
}