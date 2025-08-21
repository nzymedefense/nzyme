package app.nzyme.core.gnss;

import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;

import java.util.ArrayList;
import java.util.Optional;

public class GNSSRegistryKeys {

    public static final RegistryKey GNSS_RETENTION_TIME_DAYS = RegistryKey.create(
            "gnss_retention_time_days",
            Optional.of(new ArrayList<>() {{
                add(ConfigurationEntryConstraint.createNumberRangeConstraint(1, Integer.MAX_VALUE));
            }}),
            Optional.of("30"),
            false
    );

}
