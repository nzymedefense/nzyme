package app.nzyme.core.rest.responses.dot11.monitoring.ssids;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SSIDMonitoringConfiguration {

    @JsonProperty("is_enabled")
    public abstract boolean isEnabled();

    public static SSIDMonitoringConfiguration create(boolean newEnabled) {
        return builder()
                .setEnabled(newEnabled)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDMonitoringConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setEnabled(boolean newEnabled);

        public abstract SSIDMonitoringConfiguration build();
    }
}
