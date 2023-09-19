package app.nzyme.core.rest.responses.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class EventActionDetailsResponse {

    @JsonProperty("id")
    public abstract UUID id();

    @JsonProperty("organization_id")
    @Nullable
    public abstract UUID organizationId();

    @JsonProperty("action_type")
    public abstract String actionType();

    @JsonProperty("action_type_human_readable")
    public abstract String actionTypeHumanReadable();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("description")
    public abstract String description();

    @JsonProperty("configuration")
    public abstract Map<String, Object> configuration();

    @JsonProperty("subscribed_to_system_events")
    public abstract List<SystemEventTypeDetailsResponse> subscribedToSystemEvents();

    @JsonProperty("subscribed_to_detection_events")
    public abstract List<DetectionEventTypeDetailsResponse> subscribedToDetectionEvents();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    public static EventActionDetailsResponse create(UUID id, UUID organizationId, String actionType, String actionTypeHumanReadable, String name, String description, Map<String, Object> configuration, List<SystemEventTypeDetailsResponse> subscribedToSystemEvents, List<DetectionEventTypeDetailsResponse> subscribedToDetectionEvents, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .organizationId(organizationId)
                .actionType(actionType)
                .actionTypeHumanReadable(actionTypeHumanReadable)
                .name(name)
                .description(description)
                .configuration(configuration)
                .subscribedToSystemEvents(subscribedToSystemEvents)
                .subscribedToDetectionEvents(subscribedToDetectionEvents)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EventActionDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(UUID id);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder actionType(String actionType);

        public abstract Builder actionTypeHumanReadable(String actionTypeHumanReadable);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder configuration(Map<String, Object> configuration);

        public abstract Builder subscribedToSystemEvents(List<SystemEventTypeDetailsResponse> subscribedToSystemEvents);

        public abstract Builder subscribedToDetectionEvents(List<DetectionEventTypeDetailsResponse> subscribedToDetectionEvents);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract EventActionDetailsResponse build();
    }
}
