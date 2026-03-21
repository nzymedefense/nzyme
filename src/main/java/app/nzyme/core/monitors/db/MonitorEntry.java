package app.nzyme.core.monitors.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class MonitorEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract UUID organizationId();
    public abstract UUID tenantId();
    public abstract boolean enabled();
    public abstract String type();
    public abstract String name();
    @Nullable
    public abstract String description();
    @Nullable
    public abstract List<UUID> taps();
    public abstract int triggerCondition();
    public abstract int interval();
    public abstract String filters();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static MonitorEntry create(long id, UUID uuid, UUID organizationId, UUID tenantId, boolean enabled, String type, String name, String description, List<UUID> taps, int triggerCondition, int interval, String filters, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .enabled(enabled)
                .type(type)
                .name(name)
                .description(description)
                .taps(taps)
                .triggerCondition(triggerCondition)
                .interval(interval)
                .filters(filters)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitorEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder enabled(boolean enabled);

        public abstract Builder type(String type);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder taps(List<UUID> taps);

        public abstract Builder triggerCondition(int triggerCondition);

        public abstract Builder interval(int interval);

        public abstract Builder filters(String filters);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract MonitorEntry build();
    }
}
