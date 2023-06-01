package app.nzyme.core.rest.responses.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class EventActionDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("organization_id")
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

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    public static EventActionDetailsResponse create(UUID uuid, UUID organizationId, String actionType, String actionTypeHumanReadable, String name, String description, Map<String, Object> configuration, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .uuid(uuid)
                .organizationId(organizationId)
                .actionType(actionType)
                .actionTypeHumanReadable(actionTypeHumanReadable)
                .name(name)
                .description(description)
                .configuration(configuration)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EventActionDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder actionType(String actionType);

        public abstract Builder actionTypeHumanReadable(String actionTypeHumanReadable);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder configuration(Map<String, Object> configuration);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract EventActionDetailsResponse build();
    }
}
