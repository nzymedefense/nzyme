package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class BeaconSourcesListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("sources")
    public abstract List<BeaconSourceResponse> sources();

    public static BeaconSourcesListResponse create(long total, List<BeaconSourceResponse> sources) {
        return builder()
                .total(total)
                .sources(sources)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconSourcesListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder sources(List<BeaconSourceResponse> sources);

        public abstract BeaconSourcesListResponse build();
    }
}
