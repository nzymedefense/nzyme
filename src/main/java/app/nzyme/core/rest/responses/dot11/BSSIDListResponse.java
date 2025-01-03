package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@AutoValue
public abstract class BSSIDListResponse {

    @JsonProperty("total")
    @NotNull
    public abstract Long total();

    @JsonProperty("bssids")
    public abstract List<BSSIDSummaryDetailsResponse> bssids();

    public static BSSIDListResponse create(Long total, List<BSSIDSummaryDetailsResponse> bssids) {
        return builder()
                .total(total)
                .bssids(bssids)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(Long total);

        public abstract Builder bssids(List<BSSIDSummaryDetailsResponse> bssids);

        public abstract BSSIDListResponse build();
    }
}
