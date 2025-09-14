package app.nzyme.core.connect.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ConnectNodeTimerReport {

    @JsonProperty("max")
    public abstract double max();
    @JsonProperty("min")
    public abstract double min();
    @JsonProperty("mean")
    public abstract double mean();
    @JsonProperty("p99")
    public abstract double p99();
    @JsonProperty("stddev")
    public abstract double stddev();
    @JsonProperty("counter")
    public abstract double counter();

    public static ConnectNodeTimerReport create(double max, double min, double mean, double p99, double stddev, double counter) {
        return builder()
                .max(max)
                .min(min)
                .mean(mean)
                .p99(p99)
                .stddev(stddev)
                .counter(counter)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectNodeTimerReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder max(double max);

        public abstract Builder min(double min);

        public abstract Builder mean(double mean);

        public abstract Builder p99(double p99);

        public abstract Builder stddev(double stddev);

        public abstract Builder counter(double counter);

        public abstract ConnectNodeTimerReport build();
    }
}
