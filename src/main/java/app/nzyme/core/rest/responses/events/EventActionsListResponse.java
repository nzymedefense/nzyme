package app.nzyme.core.rest.responses.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class EventActionsListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("actions")
    public abstract List<EventActionDetailsResponse> actions();

    public static EventActionsListResponse create(long count, List<EventActionDetailsResponse> actions) {
        return builder()
                .count(count)
                .actions(actions)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EventActionsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder actions(List<EventActionDetailsResponse> actions);

        public abstract EventActionsListResponse build();
    }
}
