package horse.wtf.nzyme.monitoring.prometheus;

import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;

import java.util.ArrayList;
import java.util.Optional;

public class PrometheusRegistryKeys {

    public static final RegistryKey REST_REPORT_ENABLED = RegistryKey.create(
            "prometheus_rest_report_enabled",
            Optional.of(new ArrayList<>(){{
                add(ConfigurationEntryConstraint.createSimpleBooleanConstraint());
            }}),
            Optional.of("false"),
            false
    );

    public static final RegistryKey REST_REPORT_USERNAME = RegistryKey.create(
            "prometheus_rest_report_username",
            Optional.of(new ArrayList<>(){{
                add(ConfigurationEntryConstraint.createStringLengthConstraint(1, 255));
            }}),
            Optional.empty(),
            false
    );

    public static final RegistryKey REST_REPORT_PASSWORD = RegistryKey.create(
            "prometheus_rest_report_password",
            Optional.of(new ArrayList<>(){{
                add(ConfigurationEntryConstraint.createStringLengthConstraint(1, 255));
            }}),
            Optional.empty(),
            false
    );

}
