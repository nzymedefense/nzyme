package horse.wtf.nzyme.monitoring.prometheus;

import app.nzyme.plugin.RegistryKey;
import app.nzyme.plugin.rest.configuration.ConfigurationEntryConstraint;

import java.util.ArrayList;
import java.util.Optional;

public class RegistryKeys {

    public static final RegistryKey PROMETHEUS_REST_METRICS_ENABLED = RegistryKey.create(
            "prometheus_rest_metrics_enabled",
            Optional.of(new ArrayList<>() {{
                ConfigurationEntryConstraint.createSimpleBooleanConstraint();
            }}),
            Optional.of("false"),
            false
    );

}
