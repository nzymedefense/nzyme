package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class BeaconsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("classification_counts")
    public abstract Map<String, Long> classificationCounts();

    @JsonProperty("beacons")
    public abstract List<BeaconDetailsResponse> beacons();

    public static BeaconsListResponse create(long total, Map<String, Long> classificationCounts, List<BeaconDetailsResponse> beacons) {
        return builder()
                .total(total)
                .classificationCounts(classificationCounts)
                .beacons(beacons)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder classificationCounts(Map<String, Long> classificationCounts);

        public abstract Builder beacons(List<BeaconDetailsResponse> beacons);

        public abstract BeaconsListResponse build();
    }
}
