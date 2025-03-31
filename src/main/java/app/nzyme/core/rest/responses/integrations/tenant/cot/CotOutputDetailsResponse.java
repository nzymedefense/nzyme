package app.nzyme.core.rest.responses.integrations.tenant.cot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class CotOutputDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("organization_id")
    public abstract UUID organizationId();

    @JsonProperty("tenant_id")
    public abstract UUID tenantId();

    @JsonProperty("connection_type")
    public abstract String connectionType();

    @JsonProperty("name")
    public abstract String name();

    @Nullable
    @JsonProperty("description")
    public abstract String description();

    @JsonProperty("leaf_type_tap")
    public abstract String leafTypeTap();

    @JsonProperty("address")
    public abstract String address();

    @JsonProperty("port")
    public abstract int port();

    @JsonProperty("status")
    public abstract String status();

    @JsonProperty("sent_messages")
    public abstract long sentMessages();

    @JsonProperty("sent_bytes")
    public abstract long sentBytes();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static CotOutputDetailsResponse create(UUID uuid, UUID organizationId, UUID tenantId, String connectionType, String name, String description, String leafTypeTap, String address, int port, String status, long sentMessages, long sentBytes, DateTime updatedAt, DateTime createdAt) {
        return builder()
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .connectionType(connectionType)
                .name(name)
                .description(description)
                .leafTypeTap(leafTypeTap)
                .address(address)
                .port(port)
                .status(status)
                .sentMessages(sentMessages)
                .sentBytes(sentBytes)
                .updatedAt(updatedAt)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CotOutputDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder connectionType(String connectionType);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder leafTypeTap(String leafTypeTap);

        public abstract Builder address(String address);

        public abstract Builder port(int port);

        public abstract Builder status(String status);

        public abstract Builder sentMessages(long sentMessages);

        public abstract Builder sentBytes(long sentBytes);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract CotOutputDetailsResponse build();
    }
}
