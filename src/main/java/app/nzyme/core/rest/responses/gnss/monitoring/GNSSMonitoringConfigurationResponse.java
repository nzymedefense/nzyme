package app.nzyme.core.rest.responses.gnss.monitoring;

import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class GNSSMonitoringConfigurationResponse {

    @JsonProperty("gnss_monitoring_training_period_minutes")
    public abstract ConfigurationEntryResponse transportStrategy();

    public static GNSSMonitoringConfigurationResponse create(ConfigurationEntryResponse transportStrategy) {
        return builder()
                .transportStrategy(transportStrategy)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSMonitoringConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder transportStrategy(ConfigurationEntryResponse transportStrategy);

        public abstract GNSSMonitoringConfigurationResponse build();
    }

}
