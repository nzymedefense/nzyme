package app.nzyme.core.detection.alerts;

import app.nzyme.plugin.Subsystem;

public enum DetectionType {

    // Assets.
    ASSETS_NEW("New asset detected", Subsystem.ETHERNET),
    ASSETS_DHCP_FINGERPRINT_NEW("Asset is presenting a new DHCP fingerprint", Subsystem.ETHERNET),

    // Dot11 Monitor alerts.
    DOT11_MONITOR_BSSID("WiFi Network Monitor: Unexpected BSSID detected", Subsystem.DOT11),
    DOT11_MONITOR_CHANNEL("WiFi Network Monitor: Unexpected channel usage detected", Subsystem.DOT11),
    DOT11_MONITOR_SECURITY_SUITE("WiFi Network Monitor: Unexpected security suite configuration detected", Subsystem.DOT11),
    DOT11_MONITOR_FINGERPRINT("WiFi Network Monitor: Unexpected BSSID fingerprint detected", Subsystem.DOT11),
    DOT11_MONITOR_SIGNAL_TRACK("WiFi Network Monitor: Multiple BSSID signal tracks detected", Subsystem.DOT11),
    DOT11_MONITOR_DISCO_ANOMALIES("WiFi Network Monitor: Disconnection Event Anomalies detected", Subsystem.DOT11),
    DOT11_MONITOR_SIMILAR_LOOKING_SSID("WiFi Network Monitor: SSID with a similar name to the monitored SSID detected", Subsystem.DOT11),
    DOT11_MONITOR_SSID_SUBSTRING("WiFi Network Monitor: SSID includes a forbidden substring", Subsystem.DOT11),

    // Other Dot11 alerts.
    DOT11_BANDIT_CONTACT("WiFi Bandit detected", Subsystem.DOT11),
    DOT11_PROBEREQ("Monitored probe request detected", Subsystem.DOT11),
    DOT11_UNAPPROVED_SSID("Unapproved SSID detected", Subsystem.DOT11),
    DOT11_UNAPPROVED_CLIENT("Unapproved WiFi Client detected", Subsystem.DOT11),

    // UAV alerts.
    UAV_DETECTED_CLASSIFICATION_UNKNOWN("UAV with unknown classification detected", Subsystem.UAV),
    UAV_DETECTED_CLASSIFICATION_FRIENDLY("UAV with friendly classification detected", Subsystem.UAV),
    UAV_DETECTED_CLASSIFICATION_NEUTRAL("UAV with neutral classification detected", Subsystem.UAV),
    UAV_DETECTED_CLASSIFICATION_HOSTILE("UAV with hostile classification detected", Subsystem.UAV),

    // Wildcard subscription.
    WILDCARD("Subscribed to all detection alerts. (Wildcard)", Subsystem.GENERIC);

    private final String title;
    private final Subsystem subsystem;

    DetectionType(String title, Subsystem subsystem) {
        this.title = title;
        this.subsystem = subsystem;
    }

    public String getTitle() {
        return title;
    }

    public Subsystem getSubsystem() {
        return subsystem;
    }

}
