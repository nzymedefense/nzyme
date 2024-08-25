package app.nzyme.core.dot11.db.monitoring.probereq;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class MonitoredProbeRequestEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract UUID organizationId();
    public abstract UUID tenantId();
    public abstract String ssid();
    @Nullable
    public abstract String notes();
    public abstract DateTime updatedAt();
    public abstract DateTime createdAt();

    public static MonitoredProbeRequestEntry create(long id, UUID uuid, UUID organizationId, UUID tenantId, String ssid, String notes, DateTime updatedAt, DateTime createdAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .ssid(ssid)
                .notes(notes)
                .updatedAt(updatedAt)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredProbeRequestEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder ssid(String ssid);

        public abstract Builder notes(String notes);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract MonitoredProbeRequestEntry build();
    }
}
