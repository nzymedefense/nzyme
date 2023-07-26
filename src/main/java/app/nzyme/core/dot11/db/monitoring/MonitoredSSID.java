package app.nzyme.core.dot11.db.monitoring;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class MonitoredSSID {

    public abstract long id();
    public abstract UUID uuid();
    public abstract boolean isEnabled();
    public abstract String ssid();
    @Nullable
    public abstract UUID organizationId();
    @Nullable
    public abstract UUID tenantId();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static MonitoredSSID create(long id, UUID uuid, boolean isEnabled, String ssid, UUID organizationId, UUID tenantId, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .isEnabled(isEnabled)
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
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder isEnabled(boolean isEnabled);

        public abstract Builder ssid(String ssid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract MonitoredSSID build();
    }
}
