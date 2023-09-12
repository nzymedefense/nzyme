package app.nzyme.core.detection.alerts;

public enum DetectionType {

    // Dot11 Monitor alerts.
    DOT11_MONITOR_BSSID("WiFi Network Monitor: Unexpected BSSID detected", Subsystem.DOT11),
    DOT11_MONITOR_CHANNEL("WiFi Network Monitor: Unexpected channel usage detected", Subsystem.DOT11),
    DOT11_MONITOR_SECURITY_SUITE("WiFi Network Monitor: Unexpected security suite configuration detected", Subsystem.DOT11),
    DOT11_MONITOR_FINGERPRINT("WiFi Network Monitor: Unexpected BSSID fingerprint detected", Subsystem.DOT11),
    DOT11_MONITOR_SIGNAL_TRACK("WiFi Network Monitor: Multiple BSSID signal tracks detected", Subsystem.DOT11),

    // Other Dot11 alerts.
    DOT11_BANDIT_CONTACT("WiFi Bandit detected", Subsystem.DOT11),

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
