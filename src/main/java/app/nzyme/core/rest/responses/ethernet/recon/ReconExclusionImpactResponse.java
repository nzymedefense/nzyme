package app.nzyme.core.rest.responses.ethernet.recon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Which existing scans an exclusion applies to.")
@AutoValue
public abstract class ReconExclusionImpactResponse {

    @Schema(description = "Existing scans that match the exclusion. Applying the exclusion deletes them.")
    @JsonProperty("matching_scans")
    public abstract int matchingScans();

    @Schema(description = "A sample of the matching scans. Limited to 25 entries.")
    @JsonProperty("scans")
    public abstract List<ReconExclusionMatchedScanResponse> scans();

    public static ReconExclusionImpactResponse create(int matchingScans, List<ReconExclusionMatchedScanResponse> scans) {
        return builder()
                .matchingScans(matchingScans)
                .scans(scans)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconExclusionImpactResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder matchingScans(int matchingScans);
        public abstract Builder scans(List<ReconExclusionMatchedScanResponse> scans);
        public abstract ReconExclusionImpactResponse build();
    }
}
