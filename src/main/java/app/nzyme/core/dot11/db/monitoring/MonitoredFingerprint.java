package app.nzyme.core.dot11.db.monitoring;

import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class MonitoredFingerprint {

    public abstract long monitoredNetworkBSSIDId();
    public abstract UUID uuid();
    public abstract String fingerprint();

    public static MonitoredFingerprint create(long monitoredNetworkBSSIDId, UUID uuid, String fingerprint) {
        return builder()
                .monitoredNetworkBSSIDId(monitoredNetworkBSSIDId)
                .uuid(uuid)
                .fingerprint(fingerprint)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredFingerprint.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder monitoredNetworkBSSIDId(long monitoredNetworkBSSIDId);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder fingerprint(String fingerprint);

        public abstract MonitoredFingerprint build();
    }
}
