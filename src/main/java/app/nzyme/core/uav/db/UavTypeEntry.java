package app.nzyme.core.uav.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class UavTypeEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract UUID organizationId();
    public abstract UUID tenantId();
    public abstract String matchType();
    public abstract String matchValue();
    @Nullable
    public abstract String defaultClassification();
    public abstract String type();
    public abstract String name();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static UavTypeEntry create(long id, UUID uuid, UUID organizationId, UUID tenantId, String matchType, String matchValue, String defaultClassification, String type, String name, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .matchType(matchType)
                .matchValue(matchValue)
                .defaultClassification(defaultClassification)
                .type(type)
                .name(name)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavTypeEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder matchType(String matchType);

        public abstract Builder matchValue(String matchValue);

        public abstract Builder defaultClassification(String defaultClassification);

        public abstract Builder type(String type);

        public abstract Builder name(String name);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract UavTypeEntry build();
    }
}
