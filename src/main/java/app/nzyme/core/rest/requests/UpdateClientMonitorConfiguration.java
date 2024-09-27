package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UpdateClientMonitorConfiguration {

    public abstract boolean monitoringEnabled();

    @JsonCreator
    public static UpdateClientMonitorConfiguration create(@JsonProperty("monitoring_enabled") boolean monitoringEnabled) {
        return builder()
                .monitoringEnabled(monitoringEnabled)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateClientMonitorConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder monitoringEnabled(boolean monitoringEnabled);

        public abstract UpdateClientMonitorConfiguration build();
    }
}
