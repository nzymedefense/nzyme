package app.nzyme.core.gnss.db.monitoring;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@AutoValue
public abstract class GNSSMonitoringRuleEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract UUID organizationId();
    public abstract UUID tenantId();
    public abstract String name();
    @Nullable
    public abstract String description();
    public abstract Map<String, List<Object>> conditions();
    public abstract Optional<List<UUID>> taps();
    public abstract DateTime updatedAt();
    public abstract DateTime createdAt();

    public static GNSSMonitoringRuleEntry create(long id, UUID uuid, UUID organizationId, UUID tenantId, String name, String description, Map<String, List<Object>> conditions, Optional<List<UUID>> taps, DateTime updatedAt, DateTime createdAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .conditions(conditions)
                .taps(taps)
                .updatedAt(updatedAt)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSMonitoringRuleEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder conditions(Map<String, List<Object>> conditions);

        public abstract Builder taps(Optional<List<UUID>> taps);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract GNSSMonitoringRuleEntry build();
    }
}
