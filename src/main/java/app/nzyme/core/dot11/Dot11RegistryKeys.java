package app.nzyme.core.dot11;

import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;

import java.util.ArrayList;
import java.util.Optional;

public class Dot11RegistryKeys {

    public static final RegistryKey DOT11_RETENTION_TIME_DAYS = RegistryKey.create(
            "dot11_retention_time_days",
            Optional.of(new ArrayList<>() {{
                add(ConfigurationEntryConstraint.createNumberRangeConstraint(1, Integer.MAX_VALUE));
            }}),
            Optional.of("7"),
            false
    );

}
