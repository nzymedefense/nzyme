export default function timelineEventEnumToTitle(x) {
  switch(x) {
    case "GONE": return "Gone";
    case "DOT11_BSSID_ACTIVE_CHANNEL": return "Active Channel Change";
    case "DOT11_BSSID_STRONGEST_TAP": return "Strongest Tap Change";
    case "DOT11_BSSID_SSID_DIFF": return "SSID Change"
    case "DOT11_BSSID_FINGERPRINT_DIFF": return "Fingerprints Change"

    case "DOT11_SSID_ACTIVE_CHANNEL": return "Active Channel Change"
    case "DOT11_SSID_RATES_DIFF": return "Rates Change"
    case "DOT11_SSID_SECURITY_PROTOCOLS_DIFF": return "Security Protocols Change"
    case "DOT11_SSID_SECURITY_SUITES_DIFF": return "Security Suites Change"
    case "DOT11_SSID_FINGERPRINTS_DIFF": return "Fingerprints Change"

    default: return "Unknown"
  }
}