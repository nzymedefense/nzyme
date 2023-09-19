package app.nzyme.core.rest.responses.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DetectionEventTypeDetailsResponse {

    @JsonProperty("event")
    public abstract String event();

    @JsonProperty("title")
    public abstract String title();

    @JsonProperty("subsystem")
    public abstract String subsystem();

    public static DetectionEventTypeDetailsResponse create(String event, String title, String subsystem) {
        return builder()
                .event(event)
                .title(title)
                .subsystem(subsystem)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DetectionEventTypeDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder event(String event);

        public abstract Builder title(String title);

        public abstract Builder subsystem(String subsystem);

        public abstract DetectionEventTypeDetailsResponse build();
    }
}
