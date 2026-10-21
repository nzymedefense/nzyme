package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Which existing beacons an exclusion applies to.")
@AutoValue
public abstract class BeaconExclusionImpactResponse {

    @Schema(description = "Beacons where at least one calling device matches.")
    @JsonProperty("matching_beacons")
    public abstract int matchingBeacons();

    @Schema(description = "Beacons where every calling device matches. Applying the exclusion deletes these entirely.")
    @JsonProperty("complete_beacons")
    public abstract int completeBeacons();

    @Schema(description = "Calling devices (beacon sources) that match, across all beacons.")
    @JsonProperty("matching_sources")
    public abstract int matchingSources();

    @Schema(description = "A sample of the matching beacons, complete matches first. Limited to 25 entries.")
    @JsonProperty("beacons")
    public abstract List<BeaconExclusionMatchedBeaconResponse> beacons();

    public static BeaconExclusionImpactResponse create(int matchingBeacons, int completeBeacons, int matchingSources, List<BeaconExclusionMatchedBeaconResponse> beacons) {
        return builder()
                .matchingBeacons(matchingBeacons)
                .completeBeacons(completeBeacons)
                .matchingSources(matchingSources)
                .beacons(beacons)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconExclusionImpactResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder matchingBeacons(int matchingBeacons);
        public abstract Builder completeBeacons(int completeBeacons);
        public abstract Builder matchingSources(int matchingSources);
        public abstract Builder beacons(List<BeaconExclusionMatchedBeaconResponse> beacons);
        public abstract BeaconExclusionImpactResponse build();
    }

}
