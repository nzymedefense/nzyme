package app.nzyme.core.taps.db.metrics;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TapMetricsGaugeAggregation {

    public abstract String name();
    public abstract double value();

    public static TapMetricsGaugeAggregation create(String name, double value) {
        return builder()
                .name(name)
                .value(value)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapMetricsGaugeAggregation.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder value(double value);

        public abstract TapMetricsGaugeAggregation build();
    }
}
