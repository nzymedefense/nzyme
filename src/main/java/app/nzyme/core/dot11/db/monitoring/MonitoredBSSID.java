package app.nzyme.core.dot11.db.monitoring;

import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class MonitoredBSSID {

    public abstract long id();
    public abstract long monitoredNetworkId();
    public abstract UUID uuid();
    public abstract String bssid();

    public static MonitoredBSSID create(long id, long monitoredNetworkId, UUID uuid, String bssid) {
        return builder()
                .id(id)
                .monitoredNetworkId(monitoredNetworkId)
                .uuid(uuid)
                .bssid(bssid)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredBSSID.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder monitoredNetworkId(long monitoredNetworkId);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder bssid(String bssid);

        public abstract MonitoredBSSID build();
    }
}
