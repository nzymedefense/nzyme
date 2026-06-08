package app.nzyme.core.rest.responses.timelines;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TimelineResponse {

    @JsonProperty("retention_days")
    public abstract int retentionDays();

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("events")
    public abstract List<TimelineEventDetailsResponse> events();

    public static TimelineResponse create(int retentionDays, long total, List<TimelineEventDetailsResponse> events) {
        return builder()
                .retentionDays(retentionDays)
                .total(total)
                .events(events)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimelineResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder retentionDays(int retentionDays);

        public abstract Builder total(long total);

        public abstract Builder events(List<TimelineEventDetailsResponse> events);

        public abstract TimelineResponse build();
    }
}
