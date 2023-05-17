package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class TenantDetailsResponse {

    @JsonProperty("id")
    public abstract UUID id();

    @JsonProperty("organization_id")
    public abstract UUID organizationId();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("description")
    public abstract String description();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @JsonProperty("users_count")
    public abstract long usersCount();

    @JsonProperty("taps_count")
    public abstract long tapsCount();

    @JsonProperty("is_deletable")
    public abstract boolean isDeletable();

    public static TenantDetailsResponse create(UUID id, UUID organizationId, String name, String description, DateTime createdAt, DateTime updatedAt, long usersCount, long tapsCount, boolean isDeletable) {
        return builder()
                .id(id)
                .organizationId(organizationId)
                .name(name)
                .description(description)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .usersCount(usersCount)
                .tapsCount(tapsCount)
                .isDeletable(isDeletable)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TenantDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(UUID id);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder usersCount(long usersCount);

        public abstract Builder tapsCount(long tapsCount);

        public abstract Builder isDeletable(boolean isDeletable);

        public abstract TenantDetailsResponse build();
    }
}
