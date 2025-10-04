package app.nzyme.core.rest.responses.uav;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.List;

@AutoValue
public abstract class UavListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("map_center")
    @Nullable
    public abstract UavMapCenterResponse mapCenter();

    @JsonProperty("uavs")
    public abstract List<UavSummaryResponse> uavs();

    public static UavListResponse create(long count, UavMapCenterResponse mapCenter, List<UavSummaryResponse> uavs) {
        return builder()
                .count(count)
                .mapCenter(mapCenter)
                .uavs(uavs)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder mapCenter(UavMapCenterResponse mapCenter);

        public abstract Builder uavs(List<UavSummaryResponse> uavs);

        public abstract UavListResponse build();
    }
}
