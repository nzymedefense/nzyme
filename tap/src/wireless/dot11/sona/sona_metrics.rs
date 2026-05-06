use std::fmt;

pub const METRICS_PAYLOAD_BYTES: usize = 24;

#[derive(Debug, Clone)]
pub struct SonaMetrics {
    pub uptime_ms: u32,
    pub last_reset_reason: u32,
    pub temperature_mc: i32,
    pub frame_queue_used: u32,
    pub frame_queue_drops: u32,
    pub frame_queue_stale_drops: u32,
}

impl fmt::Display for SonaMetrics {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        let uptime_sec = self.uptime_ms as f64 / 1000.0;
        let temp_celsius = self.temperature_mc as f64 / 1000.0;
        let reset_reason_str = reset_reason_to_string(self.last_reset_reason);
        write!(
            f,
            "SonaMetrics {{ uptime: {:.2}s, temp: {:.1}°C, queue: {}, drops: {}, stale: {}, reset: {} }}",
            uptime_sec, temp_celsius, self.frame_queue_used,
            self.frame_queue_drops, self.frame_queue_stale_drops, reset_reason_str
        )
    }
}

fn reset_reason_to_string(reset_reason: u32) -> String {
    if reset_reason == 0 {
        return "NONE".to_string();
    }

    let mut reasons = Vec::new();
    if reset_reason & 0x00000001 != 0 { reasons.push("PIN_RESET"); }
    if reset_reason & 0x00000002 != 0 { reasons.push("WATCHDOG"); }
    if reset_reason & 0x00000004 != 0 { reasons.push("SOFTWARE_RESET"); }
    if reset_reason & 0x00000008 != 0 { reasons.push("CPU_LOCKUP"); }
    if reset_reason & 0x00010000 != 0 { reasons.push("WAKEUP_GPIO"); }
    if reset_reason & 0x00020000 != 0 { reasons.push("WAKEUP_LPCOMP"); }
    if reset_reason & 0x00040000 != 0 { reasons.push("DEBUG_INTERFACE"); }
    if reset_reason & 0x00080000 != 0 { reasons.push("WAKEUP_NFC"); }
    if reset_reason & 0x00100000 != 0 { reasons.push("WAKEUP_VBUS"); }

    if reasons.is_empty() {
        "UNKNOWN".to_string()
    } else {
        reasons.join(" | ")
    }
}

pub fn parse_metrics_payload(data: &[u8]) -> Option<SonaMetrics> {
    if data.len() != METRICS_PAYLOAD_BYTES {
        return None;
    }

    let uptime_ms = u32::from_le_bytes([data[0],  data[1],  data[2],  data[3]]);
    let last_reset_reason = u32::from_le_bytes([data[4],  data[5],  data[6],  data[7]]);
    let temperature_mc = i32::from_le_bytes([data[8],  data[9],  data[10], data[11]]);
    let frame_queue_used = u32::from_le_bytes([data[12], data[13], data[14], data[15]]);
    let frame_queue_drops = u32::from_le_bytes([data[16], data[17], data[18], data[19]]);
    let frame_queue_stale_drops = u32::from_le_bytes([data[20], data[21], data[22], data[23]]);

    Some(SonaMetrics {
        uptime_ms,
        last_reset_reason,
        temperature_mc,
        frame_queue_used,
        frame_queue_drops,
        frame_queue_stale_drops,
    })
}