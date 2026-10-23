package app.nzyme.core.rest.responses.ethernet.recon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ReconScanPortRangeResponse {

    @JsonProperty("range")
    public abstract String range();

    @JsonProperty("probed")
    public abstract long probed();

    @JsonProperty("answered")
    public abstract long answered();

    public static ReconScanPortRangeResponse create(String range, long probed, long answered) {
        return builder()
                .range(range)
                .probed(probed)
                .answered(answered)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ReconScanPortRangeResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder range(String range);
        public abstract Builder probed(long probed);
        public abstract Builder answered(long answered);
        public abstract ReconScanPortRangeResponse build();
    }
}
