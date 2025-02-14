package app.nzyme.core.rest.responses.uav;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class UavListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("uavs")
    public abstract List<UavDetailsResponse> uavs();

    public static UavListResponse create(long count, List<UavDetailsResponse> uavs) {
        return builder()
                .count(count)
                .uavs(uavs)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder uavs(List<UavDetailsResponse> uavs);

        public abstract UavListResponse build();
    }
}
