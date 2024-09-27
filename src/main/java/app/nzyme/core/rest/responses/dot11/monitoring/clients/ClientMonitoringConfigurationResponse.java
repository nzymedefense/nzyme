package app.nzyme.core.rest.responses.dot11.monitoring.clients;

import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ClientMonitoringConfigurationResponse {

    @JsonProperty("is_enabled")
    public abstract ConfigurationEntryResponse isEnabled();

    @JsonProperty("eventing_is_enabled")
    public abstract ConfigurationEntryResponse eventingIsEnabled();

    public static ClientMonitoringConfigurationResponse create(ConfigurationEntryResponse isEnabled, ConfigurationEntryResponse eventingIsEnabled) {
        return builder()
                .isEnabled(isEnabled)
                .eventingIsEnabled(eventingIsEnabled)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClientMonitoringConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder isEnabled(ConfigurationEntryResponse isEnabled);

        public abstract Builder eventingIsEnabled(ConfigurationEntryResponse eventingIsEnabled);

        public abstract ClientMonitoringConfigurationResponse build();
    }

}
