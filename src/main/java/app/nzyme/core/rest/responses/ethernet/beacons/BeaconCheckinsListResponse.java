package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class BeaconCheckinsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("checkins")
    public abstract List<BeaconCheckinResponse> checkins();

    public static BeaconCheckinsListResponse create(long total, List<BeaconCheckinResponse> checkins) {
        return builder()
                .total(total)
                .checkins(checkins)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconCheckinsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder checkins(List<BeaconCheckinResponse> checkins);

        public abstract BeaconCheckinsListResponse build();
    }
}
