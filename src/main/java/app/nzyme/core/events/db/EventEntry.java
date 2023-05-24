package app.nzyme.core.events.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class EventEntry {

    public abstract UUID uuid();
    public abstract UUID organizationId();
    public abstract UUID tenantId();
    public abstract String eventType();
    public abstract String reference();
    @Nullable
    public abstract String actionsFired();
    public abstract String details();
    public abstract DateTime createdAt();

    public static EventEntry create(UUID uuid, UUID organizationId, UUID tenantId, String eventType, String reference, String actionsFired, String details, DateTime createdAt) {
        return builder()
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .eventType(eventType)
                .reference(reference)
                .actionsFired(actionsFired)
                .details(details)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EventEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder eventType(String eventType);

        public abstract Builder reference(String reference);

        public abstract Builder actionsFired(String actionsFired);

        public abstract Builder details(String details);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract EventEntry build();
    }
}
