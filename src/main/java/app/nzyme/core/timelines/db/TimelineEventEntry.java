package app.nzyme.core.timelines.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@AutoValue
public abstract class TimelineEventEntry {

    @Nullable // For synthetic events.
    public abstract Long id();
    public abstract UUID uuid();
    public abstract UUID organizationId();
    public abstract UUID tenantId();
    public abstract String address();
    public abstract String addressType();
    public abstract String eventType();
    public abstract String eventDetails();
    public abstract DateTime timestamp();

    public static TimelineEventEntry create(@Nullable Long id, UUID uuid, UUID organizationId, UUID tenantId, String address, String addressType, String eventType, String eventDetails, DateTime timestamp) {
        return builder()
                .id(id)
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .address(address)
                .addressType(addressType)
                .eventType(eventType)
                .eventDetails(eventDetails)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimelineEventEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(@Nullable Long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder address(String address);

        public abstract Builder addressType(String addressType);

        public abstract Builder eventType(String eventType);

        public abstract Builder eventDetails(String eventDetails);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract TimelineEventEntry build();
    }
}
