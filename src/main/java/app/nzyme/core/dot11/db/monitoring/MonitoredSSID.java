package app.nzyme.core.dot11.db.monitoring;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class MonitoredSSID {

    public abstract UUID uuid();
    public abstract String ssid();
    @Nullable
    public abstract UUID organizationId();
    @Nullable
    public abstract UUID tenantId();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static MonitoredSSID create(UUID uuid, String ssid, UUID organizationId, UUID tenantId, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .uuid(uuid)
                .ssid(ssid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredSSID.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder ssid(String ssid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract MonitoredSSID build();
    }
}
