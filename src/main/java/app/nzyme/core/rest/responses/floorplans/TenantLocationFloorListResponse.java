package app.nzyme.core.rest.responses.floorplans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TenantLocationFloorListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("floors")
    public abstract List<TenantLocationFloorDetailsResponse> floors();

    public static TenantLocationFloorListResponse create(long count, List<TenantLocationFloorDetailsResponse> floors) {
        return builder()
                .count(count)
                .floors(floors)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TenantLocationFloorListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder floors(List<TenantLocationFloorDetailsResponse> floors);

        public abstract TenantLocationFloorListResponse build();
    }
}
