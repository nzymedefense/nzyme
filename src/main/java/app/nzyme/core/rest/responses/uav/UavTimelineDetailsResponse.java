package app.nzyme.core.rest.responses.uav;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class UavTimelineDetailsResponse {

    @JsonProperty("is_active")
    public abstract boolean isActive();

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("seen_from")
    public abstract DateTime seenFrom();

    @JsonProperty("seen_to")
    public abstract DateTime seenTo();

    public static UavTimelineDetailsResponse create(boolean isActive, UUID uuid, DateTime seenFrom, DateTime seenTo) {
        return builder()
                .isActive(isActive)
                .uuid(uuid)
                .seenFrom(seenFrom)
                .seenTo(seenTo)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavTimelineDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder isActive(boolean isActive);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder seenFrom(DateTime seenFrom);

        public abstract Builder seenTo(DateTime seenTo);

        public abstract UavTimelineDetailsResponse build();
    }
}
