package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TenantsListResponse {

    @JsonProperty("tenants")
    public abstract List<TenantDetailsResponse> tenants();

    public static TenantsListResponse create(List<TenantDetailsResponse> tenants) {
        return builder()
                .tenants(tenants)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TenantsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tenants(List<TenantDetailsResponse> tenants);

        public abstract TenantsListResponse build();
    }

}
