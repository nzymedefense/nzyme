package app.nzyme.core.rest.resources.taps.reports.tables.dot11;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Dot11SignalStrengthReport {

    public abstract long min();
    public abstract long max();
    public abstract float average();

    @JsonCreator
    public static Dot11SignalStrengthReport create(@JsonProperty("min") long min,
                                                   @JsonProperty("max") long max,
                                                   @JsonProperty("average") float average) {
        return builder()
                .min(min)
                .max(max)
                .average(average)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11SignalStrengthReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder min(long min);

        public abstract Builder max(long max);

        public abstract Builder average(float average);

        public abstract Dot11SignalStrengthReport build();
    }
}
