package app.nzyme.core.rest.responses.taps;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TapEngagementLogsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("logs")
    public abstract List<TapEngagementLogDetailsResponse> logs();

    public static TapEngagementLogsListResponse create(long total, List<TapEngagementLogDetailsResponse> logs) {
        return builder()
                .total(total)
                .logs(logs)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapEngagementLogsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder logs(List<TapEngagementLogDetailsResponse> logs);

        public abstract TapEngagementLogsListResponse build();
    }
}
