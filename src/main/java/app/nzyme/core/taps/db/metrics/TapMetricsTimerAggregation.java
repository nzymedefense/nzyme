package app.nzyme.core.taps.db.metrics;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TapMetricsTimerAggregation {

    public abstract String name();
    public abstract double mean();
    public abstract double p99();

    public static TapMetricsTimerAggregation create(String name, double mean, double p99) {
        return builder()
                .name(name)
                .mean(mean)
                .p99(p99)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapMetricsTimerAggregation.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder mean(double mean);

        public abstract Builder p99(double p99);

        public abstract TapMetricsTimerAggregation build();
    }
}
