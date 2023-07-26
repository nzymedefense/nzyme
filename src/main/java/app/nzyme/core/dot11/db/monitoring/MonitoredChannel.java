package app.nzyme.core.dot11.db.monitoring;

import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class MonitoredChannel {

    public abstract long monitoredNetworkId();
    public abstract UUID uuid();
    public abstract long frequency();

    public static MonitoredChannel create(long monitoredNetworkId, UUID uuid, long frequency) {
        return builder()
                .monitoredNetworkId(monitoredNetworkId)
                .uuid(uuid)
                .frequency(frequency)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredChannel.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder monitoredNetworkId(long monitoredNetworkId);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder frequency(long frequency);

        public abstract MonitoredChannel build();
    }
}
