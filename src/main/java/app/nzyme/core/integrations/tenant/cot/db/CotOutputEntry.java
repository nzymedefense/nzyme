package app.nzyme.core.integrations.tenant.cot.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class CotOutputEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract UUID organizationId();
    public abstract UUID tenantId();

    public abstract String name();
    @Nullable
    public abstract String description();

    public abstract String leafTypeTap();
    public abstract String leafTypeUav();

    public abstract String address();
    public abstract int port();

    public abstract DateTime updatedAt();
    public abstract DateTime createdAt();

    public static CotOutputEntry create(long id, UUID uuid, UUID organizationId, UUID tenantId, String name, String description, String leafTypeTap, String leafTypeUav, String address, int port, DateTime updatedAt, DateTime createdAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .leafTypeTap(leafTypeTap)
                .leafTypeUav(leafTypeUav)
                .address(address)
                .port(port)
                .updatedAt(updatedAt)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CotOutputEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder leafTypeTap(String leafTypeTap);

        public abstract Builder leafTypeUav(String leafTypeUav);

        public abstract Builder address(String address);

        public abstract Builder port(int port);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract CotOutputEntry build();
    }
}
