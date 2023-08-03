package app.nzyme.core.rest.responses.dot11.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class MonitoredAttributeResult {

    @JsonProperty("triggered")
    public abstract boolean triggered();

    @JsonProperty("deviated_values")
    @Nullable
    public abstract Object deviatedValues();

    public static MonitoredAttributeResult create(boolean triggered, Object deviatedValues) {
        return builder()
                .triggered(triggered)
                .deviatedValues(deviatedValues)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredAttributeResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder triggered(boolean triggered);

        public abstract Builder deviatedValues(Object deviatedValues);

        public abstract MonitoredAttributeResult build();
    }
}
