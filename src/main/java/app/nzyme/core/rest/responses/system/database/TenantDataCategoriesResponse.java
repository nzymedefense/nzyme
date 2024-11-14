package app.nzyme.core.rest.responses.system.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class TenantDataCategoriesResponse {

    public abstract UUID tenantId();
    public abstract String tenantName();

    @JsonProperty("categories")
    public abstract Map<String, DataCategorySizesAndConfigurationResponse> categories();

    public static TenantDataCategoriesResponse create(UUID tenantId, String tenantName, Map<String, DataCategorySizesAndConfigurationResponse> categories) {
        return builder()
                .tenantId(tenantId)
                .tenantName(tenantName)
                .categories(categories)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TenantDataCategoriesResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder tenantName(String tenantName);

        public abstract Builder categories(Map<String, DataCategorySizesAndConfigurationResponse> categories);

        public abstract TenantDataCategoriesResponse build();
    }
}
