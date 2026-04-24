package app.nzyme.core.rest.responses.timelines;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class TimelineEventDetailsResponse {

    @JsonProperty("id")
    @Nullable // For synthetic events.
    public abstract UUID id();
    @JsonProperty("address")
    public abstract String address();
    @JsonProperty("address_type")
    public abstract String addressType();
    @JsonProperty("event_type")
    public abstract String eventType();
    @JsonProperty("event_details")
    public abstract Map<String, Object> eventDetails();
    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    public static TimelineEventDetailsResponse create(UUID id, String address, String addressType, String eventType, Map<String, Object> eventDetails, DateTime timestamp) {
        return builder()
                .id(id)
                .address(address)
                .addressType(addressType)
                .eventType(eventType)
                .eventDetails(eventDetails)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimelineEventDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(UUID id);

        public abstract Builder address(String address);

        public abstract Builder addressType(String addressType);

        public abstract Builder eventType(String eventType);

        public abstract Builder eventDetails(Map<String, Object> eventDetails);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract TimelineEventDetailsResponse build();
    }
}
