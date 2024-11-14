package app.nzyme.core.rest.responses.system.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class GlobalDatabaseCategoriesResponse {

    @JsonProperty("global_sizes")
    public abstract Map<String, DataCategorySizesResponse> globalSizes();

    @JsonProperty("organizations")
    public abstract List<OrganizationDataCategoriesResponse> organizations();

    public static GlobalDatabaseCategoriesResponse create(Map<String, DataCategorySizesResponse> globalSizes, List<OrganizationDataCategoriesResponse> organizations) {
        return builder()
                .globalSizes(globalSizes)
                .organizations(organizations)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GlobalDatabaseCategoriesResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder globalSizes(Map<String, DataCategorySizesResponse> globalSizes);

        public abstract Builder organizations(List<OrganizationDataCategoriesResponse> organizations);

        public abstract GlobalDatabaseCategoriesResponse build();
    }
}
