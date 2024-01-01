package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@AutoValue
public abstract class UpdateSimilarSSIDNetworkMonitorConfiguration {

    @Max(100)
    @Min(0)
    public abstract long threshold();

    @JsonCreator
    public static UpdateSimilarSSIDNetworkMonitorConfiguration create(@JsonProperty("threshold") long threshold) {
        return builder()
                .threshold(threshold)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UpdateSimilarSSIDNetworkMonitorConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder threshold(long threshold);

        public abstract UpdateSimilarSSIDNetworkMonitorConfiguration build();
    }
}
