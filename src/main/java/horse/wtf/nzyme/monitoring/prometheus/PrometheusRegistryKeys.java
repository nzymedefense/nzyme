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
            true
    );

}
