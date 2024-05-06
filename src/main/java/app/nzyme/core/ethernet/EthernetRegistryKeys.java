package app.nzyme.core.ethernet;

import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;

import java.util.ArrayList;
import java.util.Optional;

public class EthernetRegistryKeys {

    public static final RegistryKey L4_RETENTION_TIME_DAYS = RegistryKey.create(
            "ethernet_l4_retention_time_days",
            Optional.of(new ArrayList<>() {{
                add(ConfigurationEntryConstraint.createNumberRangeConstraint(1, Integer.MAX_VALUE));
            }}),
            Optional.of("7"),
            false
    );

}
