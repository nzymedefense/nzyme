package app.nzyme.core.security.authentication.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class TenantEntry {

    public abstract long id();
    public abstract long organizationId();
    public abstract String name();
    public abstract String description();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static TenantEntry create(long id, long organizationId, String name, String description, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .organizationId(organizationId)
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
        public abstract Builder id(long id);

        public abstract Builder organizationId(long organizationId);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract TenantEntry build();
    }

}

