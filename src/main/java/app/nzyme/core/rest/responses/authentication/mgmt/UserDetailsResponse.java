package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class UserDetailsResponse {

    @JsonProperty("id")
    public abstract long id();

    @JsonProperty("organization_id")
    public abstract long organization_id();

    @JsonProperty("tenant_id")
    public abstract long tenantId();

    @JsonProperty("role_id")
    @Nullable
    public abstract Long roleId();

    @JsonProperty("email")
    public abstract String email();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @JsonProperty("last_activity")
    @Nullable
    public abstract DateTime lastActivity();

    public static UserDetailsResponse create(long id, long organization_id, long tenantId, Long roleId, String email, String name, DateTime createdAt, DateTime updatedAt, DateTime lastActivity) {
        return builder()
                .id(id)
                .organization_id(organization_id)
                .tenantId(tenantId)
                .roleId(roleId)
                .email(email)
                .name(name)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .lastActivity(lastActivity)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UserDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder organization_id(long organization_id);

        public abstract Builder tenantId(long tenantId);

        public abstract Builder roleId(Long roleId);

        public abstract Builder email(String email);

        public abstract Builder name(String name);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder lastActivity(DateTime lastActivity);

        public abstract UserDetailsResponse build();
    }
}
