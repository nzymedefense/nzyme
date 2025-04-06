package app.nzyme.core.rest.responses.uav;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UavDetailsResponse {

    @JsonProperty("summary")
    public abstract UavSummaryResponse summary();

    public static UavDetailsResponse create(UavSummaryResponse summary) {
        return builder()
                .summary(summary)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder summary(UavSummaryResponse summary);

        public abstract UavDetailsResponse build();
    }
}
