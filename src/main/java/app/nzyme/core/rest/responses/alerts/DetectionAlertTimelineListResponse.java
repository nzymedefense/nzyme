package app.nzyme.core.rest.responses.alerts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class DetectionAlertTimelineListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("entries")
    public abstract List<DetectionAlertTimelineDetailsResponse> entries();

    public static DetectionAlertTimelineListResponse create(long total, List<DetectionAlertTimelineDetailsResponse> entries) {
        return builder()
                .total(total)
                .entries(entries)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DetectionAlertTimelineListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder entries(List<DetectionAlertTimelineDetailsResponse> entries);

        public abstract DetectionAlertTimelineListResponse build();
    }
}
