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

    public static final RegistryKey GNSS_MONITORING_TRAINING_PERIOD_MINUTES = RegistryKey.create(
            "gnss_monitoring_training_period_minutes",
            Optional.of(new ArrayList<>() {{
                add(ConfigurationEntryConstraint.createNumberRangeConstraint(0, Integer.MAX_VALUE));
            }}),
            Optional.of("10"),
            false
    );

}
