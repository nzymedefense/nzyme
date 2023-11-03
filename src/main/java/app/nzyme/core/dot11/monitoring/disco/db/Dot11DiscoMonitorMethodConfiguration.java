package app.nzyme.core.dot11.monitoring.disco.db;

import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class Dot11DiscoMonitorMethodConfiguration {

    public abstract String type();
    public abstract Map<String, Object> configuration();

    public static Dot11DiscoMonitorMethodConfiguration create(String type, Map<String, Object> configuration) {
        return builder()
                .type(type)
                .configuration(configuration)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11DiscoMonitorMethodConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(String type);

        public abstract Builder configuration(Map<String, Object> configuration);

        public abstract Dot11DiscoMonitorMethodConfiguration build();
    }
}
