package app.nzyme.core.rest.responses.ethernet.recon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class ReconScansListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("type_counts")
    public abstract Map<String, Long> typeCounts();

    @JsonProperty("scans")
    public abstract List<ReconScanDetailsResponse> scans();

    public static ReconScansListResponse create(long total, Map<String, Long> typeCounts, List<ReconScanDetailsResponse> scans) {
        return builder()
                .total(total)
                .typeCounts(typeCounts)
                .scans(scans)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconScansListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);
        public abstract Builder typeCounts(Map<String, Long> typeCounts);
        public abstract Builder scans(List<ReconScanDetailsResponse> scans);
        public abstract ReconScansListResponse build();
    }
}
