package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TenantsListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("tenants")
    public abstract List<TenantDetailsResponse> tenants();

    public static TenantsListResponse create(long count, List<TenantDetailsResponse> tenants) {
        return builder()
                .count(count)
                .tenants(tenants)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TenantsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder tenants(List<TenantDetailsResponse> tenants);

        public abstract TenantsListResponse build();
    }
}
