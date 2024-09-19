package app.nzyme.core.rest.responses.dot11.monitoring.ssids;

import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SSIDMonitoringConfigurationResponse {

    @JsonProperty("is_enabled")
    public abstract ConfigurationEntryResponse isEnabled();

    public static SSIDMonitoringConfigurationResponse create(ConfigurationEntryResponse isEnabled) {
        return builder()
                .isEnabled(isEnabled)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDMonitoringConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder isEnabled(ConfigurationEntryResponse isEnabled);

        public abstract SSIDMonitoringConfigurationResponse build();
    }
}
