package app.nzyme.core.connect.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ConnectTapTimerReport {

    @JsonProperty("mean")
    public abstract double mean();

    @JsonProperty("p99")
    public abstract double p99();

    public static ConnectTapTimerReport create(double mean, double p99) {
        return builder()
                .mean(mean)
                .p99(p99)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectTapTimerReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mean(double mean);

        public abstract Builder p99(double p99);

        public abstract ConnectTapTimerReport build();
    }

}
