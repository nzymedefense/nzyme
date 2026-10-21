package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class BeaconCheckinPointsResponse {

    @JsonProperty("limit")
    public abstract int limit();

    @JsonProperty("truncated")
    public abstract boolean truncated();

    @JsonProperty("points")
    public abstract List<BeaconCheckinPointResponse> points();

    public static BeaconCheckinPointsResponse create(int limit, boolean truncated, List<BeaconCheckinPointResponse> points) {
        return builder()
                .limit(limit)
                .truncated(truncated)
                .points(points)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconCheckinPointsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder limit(int limit);
        public abstract Builder truncated(boolean truncated);
        public abstract Builder points(List<BeaconCheckinPointResponse> points);
        public abstract BeaconCheckinPointsResponse build();
    }

}
