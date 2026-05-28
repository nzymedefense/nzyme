package app.nzyme.core.floorplans.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class TenantLocationEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract UUID organizationId();
    public abstract UUID tenantId();
    public abstract String name();
    @Nullable
    public abstract String description();
    @Nullable
    public abstract Double latitude();
    @Nullable
    public abstract Double longitude();
    public abstract boolean environmentalAlertEventingEnabled();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static TenantLocationEntry create(long id, UUID uuid, UUID organizationId, UUID tenantId, String name, String description, Double latitude, Double longitude, boolean environmentalAlertEventingEnabled, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .latitude(latitude)
                .longitude(longitude)
                .environmentalAlertEventingEnabled(environmentalAlertEventingEnabled)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TenantLocationEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder latitude(Double latitude);

        public abstract Builder longitude(Double longitude);

        public abstract Builder environmentalAlertEventingEnabled(boolean environmentalAlertEventingEnabled);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract TenantLocationEntry build();
    }
}
