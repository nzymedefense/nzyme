package app.nzyme.core.rest.responses.ethernet.recon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ReconScanTargetsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("targets")
    public abstract List<ReconScanTargetResponse> targets();

    public static ReconScanTargetsListResponse create(long total, List<ReconScanTargetResponse> targets) {
        return builder()
                .total(total)
                .targets(targets)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconScanTargetsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);
        public abstract Builder targets(List<ReconScanTargetResponse> targets);
        public abstract ReconScanTargetsListResponse build();
    }
}
