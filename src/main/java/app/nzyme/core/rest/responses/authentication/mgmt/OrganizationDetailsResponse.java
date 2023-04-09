package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class OrganizationDetailsResponse {

    @JsonProperty("id")
    public abstract long id();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("description")
    public abstract String description();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @JsonProperty("tenants_count")
    public abstract long tenantsCount();

    public static OrganizationDetailsResponse create(long id, String name, String description, DateTime createdAt, DateTime updatedAt, long tenantsCount) {
        return builder()
                .id(id)
                .name(name)
                .description(description)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .tenantsCount(tenantsCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_OrganizationDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder tenantsCount(long tenantsCount);

        public abstract OrganizationDetailsResponse build();
    }

}
