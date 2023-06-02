package app.nzyme.core.events.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class EventActionEntry {

    public abstract UUID uuid();
    public abstract String actionType();
    @Nullable
    public abstract UUID organizationId();
    public abstract String name();
    public abstract String description();
    public abstract String configuration();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static EventActionEntry create(UUID uuid, String actionType, UUID organizationId, String name, String description, String configuration, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .uuid(uuid)
                .actionType(actionType)
                .organizationId(organizationId)
                .name(name)
                .description(description)
                .configuration(configuration)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EventActionEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder actionType(String actionType);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder configuration(String configuration);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract EventActionEntry build();
    }
}
