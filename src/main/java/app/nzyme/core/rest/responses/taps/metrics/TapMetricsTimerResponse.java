package app.nzyme.core.rest.responses.taps.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class TapMetricsTimerResponse {

    @JsonProperty("metric_name")
    public abstract String metricName();

    @JsonProperty("mean")
    public abstract Double mean();

    @JsonProperty("p99")
    public abstract Double p99();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static TapMetricsTimerResponse create(String metricName, Double mean, Double p99, DateTime createdAt) {
        return builder()
                .metricName(metricName)
                .mean(mean)
                .p99(p99)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapMetricsTimerResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder metricName(String metricName);

        public abstract Builder mean(Double mean);

        public abstract Builder p99(Double p99);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract TapMetricsTimerResponse build();
    }
}
