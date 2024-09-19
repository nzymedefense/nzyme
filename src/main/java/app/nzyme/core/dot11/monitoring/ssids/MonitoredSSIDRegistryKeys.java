package app.nzyme.core.dot11.monitoring.ssids;

import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;

import java.util.ArrayList;
import java.util.Optional;

public class MonitoredSSIDRegistryKeys {

    public static final RegistryKey IS_ENABLED = RegistryKey.create(
            "dot11_ssid_monitoring_enabled",
            Optional.of(new ArrayList<>() {{
                add(ConfigurationEntryConstraint.createSimpleBooleanConstraint());
            }}),
            Optional.of("false"),
            false
    );

}
