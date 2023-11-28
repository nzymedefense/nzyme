package app.nzyme.core.rest.responses.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class MacAddressContextDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("mac_address")
    public abstract String macAddress();

    @JsonProperty("name")
    public abstract String name();

    @Nullable
    @JsonProperty("description")
    public abstract String description();

    @Nullable
    @JsonProperty("notes")
    public abstract String notes();

    @JsonProperty("organization_id")
    public abstract UUID organizationId();

    @JsonProperty("organization_name")
    @Nullable
    public abstract String organizationName();

    @JsonProperty("tenant_id")
    public abstract UUID tenantId();

    @JsonProperty("tenant_name")
    @Nullable
    public abstract String tenantName();;

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    public static MacAddressContextDetailsResponse create(UUID uuid, String macAddress, String name, String description, String notes, UUID organizationId, String organizationName, UUID tenantId, String tenantName, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .uuid(uuid)
                .macAddress(macAddress)
                .name(name)
                .description(description)
                .notes(notes)
                .organizationId(organizationId)
                .organizationName(organizationName)
                .tenantId(tenantId)
                .tenantName(tenantName)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MacAddressContextDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder macAddress(String macAddress);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder notes(String notes);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder organizationName(String organizationName);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder tenantName(String tenantName);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract MacAddressContextDetailsResponse build();
    }
}
