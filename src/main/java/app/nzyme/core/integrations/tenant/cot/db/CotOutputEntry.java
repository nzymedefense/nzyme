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

    public abstract String connectionType();

    public abstract String name();
    @Nullable
    public abstract String description();

    public abstract String leafTypeTap();

    public abstract String address();
    public abstract int port();

    @Nullable
    public abstract byte[] certificate();

    public abstract String status();
    public abstract long sentMessages();
    public abstract long sentBytes();

    public abstract DateTime updatedAt();
    public abstract DateTime createdAt();

    public static CotOutputEntry create(long id, UUID uuid, UUID organizationId, UUID tenantId, String connectionType, String name, String description, String leafTypeTap, String address, int port, byte[] certificate, String status, long sentMessages, long sentBytes, DateTime updatedAt, DateTime createdAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .connectionType(connectionType)
                .name(name)
                .description(description)
                .leafTypeTap(leafTypeTap)
                .address(address)
                .port(port)
                .certificate(certificate)
                .status(status)
                .sentMessages(sentMessages)
                .sentBytes(sentBytes)
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

        public abstract Builder connectionType(String connectionType);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder leafTypeTap(String leafTypeTap);

        public abstract Builder address(String address);

        public abstract Builder port(int port);

        public abstract Builder certificate(byte[] certificate);

        public abstract Builder status(String status);

        public abstract Builder sentMessages(long sentMessages);

        public abstract Builder sentBytes(long sentBytes);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract CotOutputEntry build();
    }
}
