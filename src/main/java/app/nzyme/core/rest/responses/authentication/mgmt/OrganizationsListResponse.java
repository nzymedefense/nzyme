package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class OrganizationsListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("organizations")
    public abstract List<OrganizationDetailsResponse> organizations();

    public static OrganizationsListResponse create(long count, List<OrganizationDetailsResponse> organizations) {
        return builder()
                .count(count)
                .organizations(organizations)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_OrganizationsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder organizations(List<OrganizationDetailsResponse> organizations);

        public abstract OrganizationsListResponse build();
    }
}
