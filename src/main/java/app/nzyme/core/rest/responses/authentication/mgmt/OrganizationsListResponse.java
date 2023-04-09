package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class OrganizationsListResponse {

    @JsonProperty("organizations")
    public abstract List<OrganizationDetailsResponse> organizations();

    public static OrganizationsListResponse create(List<OrganizationDetailsResponse> organizations) {
        return builder()
                .organizations(organizations)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_OrganizationsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder organizations(List<OrganizationDetailsResponse> organizations);

        public abstract OrganizationsListResponse build();
    }

}
