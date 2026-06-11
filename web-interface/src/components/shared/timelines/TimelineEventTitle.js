export default function timelineEventEnumToTitle(x) {
  switch(x) {
    case "GONE": return "Gone";
    case "DOT11_BSSID_STRONGEST_TAP": return "Strongest Tap";
    case "DOT11_BSSID_SSID_DIFF": return "SSID Change"
    case "DOT11_BSSID_FINGERPRINT_DIFF": return "Fingerprint Change"
    default: return "Unknown"
  }
}