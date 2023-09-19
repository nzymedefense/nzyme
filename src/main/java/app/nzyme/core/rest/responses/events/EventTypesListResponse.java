package app.nzyme.core.rest.responses.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class EventTypesListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("types")
    public abstract List<SystemEventTypeDetailsResponse> types();

    public static EventTypesListResponse create(long count, List<SystemEventTypeDetailsResponse> types) {
        return builder()
                .count(count)
                .types(types)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EventTypesListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder types(List<SystemEventTypeDetailsResponse> types);

        public abstract EventTypesListResponse build();
    }
}
