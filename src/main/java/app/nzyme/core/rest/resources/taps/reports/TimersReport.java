package app.nzyme.core.rest.resources.taps.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TimersReport {

    public abstract double mean();
    public abstract double p99();

    @JsonCreator
    public static TimersReport create(@JsonProperty("mean") double mean,
                                      @JsonProperty("p99") double p99) {
        return builder()
                .mean(mean)
                .p99(p99)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimersReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mean(double mean);

        public abstract Builder p99(double p99);

        public abstract TimersReport build();
    }
}
