package app.nzyme.core.timelines.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@AutoValue
public abstract class TimelineEventEntry {

    @JsonProperty("id")
    @Nullable // For synthetic events.
    public abstract Long id();
    @JsonProperty("uuid")
    public abstract UUID uuid();
    @JsonProperty("organization_id")
    public abstract UUID organizationId();
    @JsonProperty("tenant_id")
    public abstract UUID tenantId();
    @JsonProperty("address")
    public abstract String address();
    @JsonProperty("address_Type")
    public abstract String addressType();
    @JsonProperty("event_type")
    public abstract String eventType();
    @JsonProperty("event_details")
    public abstract String eventDetails();
    @JsonProperty("timestamp")
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
