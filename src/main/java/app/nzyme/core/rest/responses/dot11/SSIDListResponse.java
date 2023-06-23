package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class SSIDListResponse {

    @JsonProperty("ssids")
    public abstract List<SSIDSummaryDetailsResponse> ssids();

    public static SSIDListResponse create(List<SSIDSummaryDetailsResponse> ssids) {
        return builder()
                .ssids(ssids)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssids(List<SSIDSummaryDetailsResponse> ssids);

        public abstract SSIDListResponse build();
    }
}
