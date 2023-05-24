package app.nzyme.core.rest.responses.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class EventDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("event_type")
    public abstract String eventType();

    @JsonProperty("reference")
    public abstract String reference();

    @JsonProperty("details")
    public abstract String details();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static EventDetailsResponse create(UUID uuid, String eventType, String reference, String details, DateTime createdAt) {
        return builder()
                .uuid(uuid)
                .eventType(eventType)
                .reference(reference)
                .details(details)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EventDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder eventType(String eventType);

        public abstract Builder reference(String reference);

        public abstract Builder details(String details);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract EventDetailsResponse build();
    }
}
