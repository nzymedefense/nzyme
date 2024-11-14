package app.nzyme.core.rest.responses.system.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class OrganizationDataCategoriesResponse {

    public abstract UUID organizationId();
    public abstract String organizationName();

    @JsonProperty("total_sizes")
    public abstract Map<String, DataCategorySizesResponse> totalSizes();

    @JsonProperty("tenants")
    public abstract List<TenantDataCategoriesResponse> tenants();

    public static OrganizationDataCategoriesResponse create(UUID organizationId, String organizationName, Map<String, DataCategorySizesResponse> totalSizes, List<TenantDataCategoriesResponse> tenants) {
        return builder()
                .organizationId(organizationId)
                .organizationName(organizationName)
                .totalSizes(totalSizes)
                .tenants(tenants)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_OrganizationDataCategoriesResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder organizationName(String organizationName);

        public abstract Builder totalSizes(Map<String, DataCategorySizesResponse> totalSizes);

        public abstract Builder tenants(List<TenantDataCategoriesResponse> tenants);

        public abstract OrganizationDataCategoriesResponse build();
    }
}
