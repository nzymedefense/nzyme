package app.nzyme.core.rest.responses.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class EventsListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("events")
    public abstract List<EventDetailsResponse> events();

    public static EventsListResponse create(long count, List<EventDetailsResponse> events) {
        return builder()
                .count(count)
                .events(events)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EventsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder events(List<EventDetailsResponse> events);

        public abstract EventsListResponse build();
    }
}
