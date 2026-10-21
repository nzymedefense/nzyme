package app.nzyme.core.ethernet.beacons;

import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;

import java.util.ArrayList;
import java.util.Optional;

public class BeaconRegistryKeys {

    public static final RegistryKey DETECTION_ENABLED = booleanKey("ethernet_beacons_detection_enabled", true);

    // Data sources.
    public static final RegistryKey SOURCE_TLS_ENABLED = booleanKey("ethernet_beacons_source_tls_enabled", true);
    public static final RegistryKey SOURCE_HTTP_ENABLED = booleanKey("ethernet_beacons_source_http_enabled", true);
    public static final RegistryKey SOURCE_SSH_ENABLED = booleanKey("ethernet_beacons_source_ssh_enabled", true);
    public static final RegistryKey SOURCE_SOCKS_ENABLED = booleanKey("ethernet_beacons_source_socks_enabled", true);
    public static final RegistryKey SOURCE_DNS_ENABLED = booleanKey("ethernet_beacons_source_dns_enabled", true);
    public static final RegistryKey SOURCE_TCP_ENABLED = booleanKey("ethernet_beacons_source_tcp_enabled", true);
    public static final RegistryKey SOURCE_UDP_ENABLED = booleanKey("ethernet_beacons_source_udp_enabled", true);

    public static final RegistryKey INCLUDE_SITE_LOCAL_DESTINATIONS =
            booleanKey("ethernet_beacons_include_site_local_destinations", true);

    // Detection parameters.
    public static final RegistryKey DETECTION_WINDOW_MINUTES =
            numberKey("ethernet_beacons_detection_window_minutes", 60, 10080, 1440);
    public static final RegistryKey MINIMUM_CHECKINS =
            numberKey("ethernet_beacons_minimum_checkins", 3, 100000, 6);
    public static final RegistryKey MINIMUM_INTERVAL_SECONDS =
            numberKey("ethernet_beacons_minimum_interval_seconds", 1, 604800, 30);
    public static final RegistryKey DNS_MINIMUM_INTERVAL_SECONDS =
            numberKey("ethernet_beacons_dns_minimum_interval_seconds", 1, 604800, 60);
    public static final RegistryKey MAXIMUM_JITTER_PERCENT =
            numberKey("ethernet_beacons_maximum_jitter_percent", 0, 100, 30);
    public static final RegistryKey MINIMUM_REGULARITY_PERCENT =
            numberKey("ethernet_beacons_minimum_regularity_percent", 1, 100, 75);

    public static final RegistryKey CHECKIN_RETENTION_TIME_DAYS =
            numberKey("ethernet_beacons_checkin_retention_time_days", 1, 3650, 30);

    // Alerting.
    public static final RegistryKey ALERT_ON_NEW_BEACON = booleanKey("ethernet_beacons_alert_on_new_beacon", false);
    public static final RegistryKey ALERT_ON_NEW_SOURCE = booleanKey("ethernet_beacons_alert_on_new_source", false);
    public static final RegistryKey ALERT_ON_SUSPICIOUS_ACTIVITY =
            booleanKey("ethernet_beacons_alert_on_suspicious_activity", false);
    public static final RegistryKey ALERT_ON_MALICIOUS_ACTIVITY =
            booleanKey("ethernet_beacons_alert_on_malicious_activity", false);

    private static RegistryKey booleanKey(String key, boolean defaultValue) {
        return RegistryKey.create(
                key,
                Optional.of(new ArrayList<>() {{
                    add(ConfigurationEntryConstraint.createSimpleBooleanConstraint());
                }}),
                Optional.of(String.valueOf(defaultValue)),
                false
        );
    }

    private static RegistryKey numberKey(String key, int min, int max, int defaultValue) {
        return RegistryKey.create(
                key,
                Optional.of(new ArrayList<>() {{
                    add(ConfigurationEntryConstraint.createNumberRangeConstraint(min, max));
                }}),
                Optional.of(String.valueOf(defaultValue)),
                false
        );
    }

}
