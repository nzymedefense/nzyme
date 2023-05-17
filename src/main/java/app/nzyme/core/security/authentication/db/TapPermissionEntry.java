package app.nzyme.core.security.authentication.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class TapPermissionEntry {

    public abstract UUID uuid();
    public abstract UUID organizationId();
    public abstract UUID tenantId();

    public abstract String name();
    public abstract String description();

    public abstract String secret();

    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    @Nullable
    public abstract DateTime lastReport();

    public static TapPermissionEntry create(UUID uuid, UUID organizationId, UUID tenantId, String name, String description, String secret, DateTime createdAt, DateTime updatedAt, DateTime lastReport) {
        return builder()
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .secret(secret)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .lastReport(lastReport)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapPermissionEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder secret(String secret);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder lastReport(DateTime lastReport);

        public abstract TapPermissionEntry build();
    }
}
