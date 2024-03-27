package app.nzyme.core.taps.db.metrics;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class TapMetricsTimer {

    public abstract String metricName();
    public abstract Double mean();
    public abstract Double p99();
    public abstract DateTime createdAt();

    public static TapMetricsTimer create(String metricName, Double mean, Double p99, DateTime createdAt) {
        return builder()
                .metricName(metricName)
                .mean(mean)
                .p99(p99)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapMetricsTimer.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder metricName(String metricName);

        public abstract Builder mean(Double mean);

        public abstract Builder p99(Double p99);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract TapMetricsTimer build();
    }
}
