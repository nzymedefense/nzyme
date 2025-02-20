package app.nzyme.core.rest.responses.uav;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UavDetailsResponse {

    @JsonProperty("summary")
    public abstract UavSummaryResponse summary();

    @JsonProperty("data_retention_days")
    public abstract int dataRetentionDays();

    public static UavDetailsResponse create(UavSummaryResponse summary, int dataRetentionDays) {
        return builder()
                .summary(summary)
                .dataRetentionDays(dataRetentionDays)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder summary(UavSummaryResponse summary);

        public abstract Builder dataRetentionDays(int dataRetentionDays);

        public abstract UavDetailsResponse build();
    }
}
