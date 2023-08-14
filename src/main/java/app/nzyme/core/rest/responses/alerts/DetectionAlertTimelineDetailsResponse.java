package app.nzyme.core.rest.responses.alerts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class DetectionAlertTimelineDetailsResponse {

    @JsonProperty("seen_from")
    public abstract DateTime seenFrom();

    @JsonProperty("seen_to")
    public abstract DateTime seenTo();

    @JsonProperty("duration_seconds")
    public abstract Long durationSeconds();

    @JsonProperty("duration_human_readable")
    public abstract String durationHumanReadable();

    public static DetectionAlertTimelineDetailsResponse create(DateTime seenFrom, DateTime seenTo, Long durationSeconds, String durationHumanReadable) {
        return builder()
                .seenFrom(seenFrom)
                .seenTo(seenTo)
                .durationSeconds(durationSeconds)
                .durationHumanReadable(durationHumanReadable)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DetectionAlertTimelineDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder seenFrom(DateTime seenFrom);

        public abstract Builder seenTo(DateTime seenTo);

        public abstract Builder durationSeconds(Long durationSeconds);

        public abstract Builder durationHumanReadable(String durationHumanReadable);

        public abstract DetectionAlertTimelineDetailsResponse build();
    }
}
