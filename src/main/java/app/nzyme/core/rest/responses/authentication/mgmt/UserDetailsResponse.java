package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class UserDetailsResponse {

    @JsonProperty("id")
    public abstract long id();

    @JsonProperty("tenant_id")
    public abstract long tenantId();

    @JsonProperty("role_id")
    public abstract long roleId();

    @JsonProperty("email")
    public abstract String email();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_ats")
    public abstract DateTime updatedAt();

    public static UserDetailsResponse create(long id, long tenantId, long roleId, String email, String name, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .tenantId(tenantId)
                .roleId(roleId)
                .email(email)
                .name(name)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UserDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder tenantId(long tenantId);

        public abstract Builder roleId(long roleId);

        public abstract Builder email(String email);

        public abstract Builder name(String name);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract UserDetailsResponse build();
    }

}
