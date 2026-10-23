package app.nzyme.core.rest.responses.ethernet.recon;

import app.nzyme.plugin.rest.configuration.ConfigurationEntryResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ReconConfigurationResponse {

    @JsonProperty("detection")
    public abstract List<ConfigurationEntryResponse> detection();

    @JsonProperty("sources")
    public abstract List<ConfigurationEntryResponse> sources();

    @JsonProperty("alerting")
    public abstract List<ConfigurationEntryResponse> alerting();

    public static ReconConfigurationResponse create(List<ConfigurationEntryResponse> detection, List<ConfigurationEntryResponse> sources, List<ConfigurationEntryResponse> alerting) {
        return builder()
                .detection(detection)
                .sources(sources)
                .alerting(alerting)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder detection(List<ConfigurationEntryResponse> detection);
        public abstract Builder sources(List<ConfigurationEntryResponse> sources);
        public abstract Builder alerting(List<ConfigurationEntryResponse> alerting);
        public abstract ReconConfigurationResponse build();
    }
}
