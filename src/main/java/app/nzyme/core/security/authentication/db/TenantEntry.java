package app.nzyme.core.security.authentication.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class TenantEntry {

    public abstract UUID uuid();
    public abstract UUID organizationUuid();
    public abstract String name();
    public abstract String description();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static TenantEntry create(UUID uuid, UUID organizationUuid, String name, String description, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .uuid(uuid)
                .organizationUuid(organizationUuid)
                .name(name)
                .description(description)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TenantEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationUuid(UUID organizationUuid);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract TenantEntry build();
    }
}

