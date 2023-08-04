package app.nzyme.core.dot11.monitoring;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Dot11NetworkMonitorResult {

    public abstract Dot11NetworkMonitorType type();
    public abstract boolean triggered();
    public abstract Object deviatedValues();

    public static Dot11NetworkMonitorResult create(Dot11NetworkMonitorType type, boolean triggered, Object deviatedValues) {
        return builder()
                .type(type)
                .triggered(triggered)
                .deviatedValues(deviatedValues)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11NetworkMonitorResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(Dot11NetworkMonitorType type);

        public abstract Builder triggered(boolean triggered);

        public abstract Builder deviatedValues(Object deviatedValues);

        public abstract Dot11NetworkMonitorResult build();
    }
}
