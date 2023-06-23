package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class BSSIDListResponse {

    @JsonProperty("bssids")
    public abstract List<BSSIDSummaryDetailsResponse> bssids();

    public static BSSIDListResponse create(List<BSSIDSummaryDetailsResponse> bssids) {
        return builder()
                .bssids(bssids)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssids(List<BSSIDSummaryDetailsResponse> bssids);

        public abstract BSSIDListResponse build();
    }
}
