package app.nzyme.core.dot11.monitoring.ssids;

import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;

import java.util.ArrayList;
import java.util.Optional;

public class KnownSSIDsRegistryKeys {

    public static final RegistryKey IS_ENABLED = RegistryKey.create(
            "dot11_ssid_monitoring_enabled",
            Optional.of(new ArrayList<>() {{
                add(ConfigurationEntryConstraint.createSimpleBooleanConstraint());
            }}),
            Optional.of("false"),
            false
    );

    public static final RegistryKey EVENTING_IS_ENABLED = RegistryKey.create(
            "dot11_ssid_monitoring_eventing_enabled",
            Optional.of(new ArrayList<>() {{
                add(ConfigurationEntryConstraint.createSimpleBooleanConstraint());
            }}),
            Optional.of("false"),
            false
    );

    public static final RegistryKey DWELL_TIME_MINUTES = RegistryKey.create(
            "dot11_ssid_monitoring_dwell_time_minutes",
            Optional.of(new ArrayList<>() {{
                add(ConfigurationEntryConstraint.createNumberRangeConstraint(0, Integer.MAX_VALUE));
            }}),
            Optional.of("5"),
            false
    );

}
