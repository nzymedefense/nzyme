package app.nzyme.core.monitoring;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class GaugeEntryAverage {

    public abstract String name();
    public abstract double value();

    public static GaugeEntryAverage create(String name, double value) {
        return builder()
                .name(name)
                .value(value)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GaugeEntryAverage.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder value(double value);

        public abstract GaugeEntryAverage build();
    }
}
