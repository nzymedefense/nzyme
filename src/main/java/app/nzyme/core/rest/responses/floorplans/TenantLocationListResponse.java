package app.nzyme.core.rest.responses.floorplans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TenantLocationListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("locations")
    public abstract List<TenantLocationDetailsResponse> locations();

    public static TenantLocationListResponse create(long count, List<TenantLocationDetailsResponse> locations) {
        return builder()
                .count(count)
                .locations(locations)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TenantLocationListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder locations(List<TenantLocationDetailsResponse> locations);

        public abstract TenantLocationListResponse build();
    }
}