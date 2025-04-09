package app.nzyme.core.uav;

import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;

import java.util.ArrayList;
import java.util.Optional;

public class UavRegistryKeys {

    public static final RegistryKey UAV_RETENTION_TIME_DAYS = RegistryKey.create(
            "uav_retention_time_days",
            Optional.of(new ArrayList<>() {{
                add(ConfigurationEntryConstraint.createNumberRangeConstraint(1, Integer.MAX_VALUE));
            }}),
            Optional.of("30"),
            false
    );

    public static final RegistryKey MONITORING_ALERT_ON_UNKNOWN = RegistryKey.create(
            "uav_monitoring_alert_on_unknown",
            Optional.of(new ArrayList<>() {{
                add(ConfigurationEntryConstraint.createSimpleBooleanConstraint());
            }}),
            Optional.of("false"),
            false
    );

    public static final RegistryKey MONITORING_ALERT_ON_FRIENDLY = RegistryKey.create(
            "uav_monitoring_alert_on_friendly",
            Optional.of(new ArrayList<>() {{
                add(ConfigurationEntryConstraint.createSimpleBooleanConstraint());
            }}),
            Optional.of("false"),
            false
    );

    public static final RegistryKey MONITORING_ALERT_ON_NEUTRAL = RegistryKey.create(
            "uav_monitoring_alert_on_neutral",
            Optional.of(new ArrayList<>() {{
                add(ConfigurationEntryConstraint.createSimpleBooleanConstraint());
            }}),
            Optional.of("false"),
            false
    );

    public static final RegistryKey MONITORING_ALERT_ON_HOSTILE = RegistryKey.create(
            "uav_monitoring_alert_on_hostile",
            Optional.of(new ArrayList<>() {{
                add(ConfigurationEntryConstraint.createSimpleBooleanConstraint());
            }}),
            Optional.of("false"),
            false
    );

}
