package app.nzyme.core.dot11.monitoring.disco.monitormethods.manualthreshold;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ManualThresholdConfiguration {

    @JsonProperty("threshold")
    public abstract int threshold();

    @JsonCreator
    public static ManualThresholdConfiguration create(@JsonProperty("threshold") int threshold) {
        return builder()
                .threshold(threshold)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ManualThresholdConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder threshold(int threshold);

        public abstract ManualThresholdConfiguration build();
    }

}
