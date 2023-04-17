package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class TapPermissionDetailsResponse {

    @JsonProperty("id")
    public abstract long id();

    @JsonProperty("organization_d")
    public abstract long organizationId();

    @JsonProperty("tenant_id")
    public abstract long tenantId();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("description")
    public abstract String description();

    @JsonProperty("secret")
    public abstract String secret();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @Nullable
    @JsonProperty("last_report")
    public abstract DateTime lastReport();

    public static TapPermissionDetailsResponse create(long id, long organizationId, long tenantId, String name, String description, String secret, DateTime createdAt, DateTime updatedAt, DateTime lastReport) {
        return builder()
                .id(id)
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
        return new AutoValue_TapPermissionDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder organizationId(long organizationId);

        public abstract Builder tenantId(long tenantId);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder secret(String secret);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder lastReport(DateTime lastReport);

        public abstract TapPermissionDetailsResponse build();
    }
}
