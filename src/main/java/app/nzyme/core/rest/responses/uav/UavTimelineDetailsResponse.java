package app.nzyme.core.rest.responses.uav;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
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

    @JsonProperty("max_distance")
    @Nullable
    public abstract Double maxDistance();

    @JsonProperty("min_distance")
    @Nullable
    public abstract Double minDistance();

    @JsonProperty("duration_seconds")
    public abstract Long durationSeconds();

    @JsonProperty("duration_human_readable")
    public abstract String durationHumanReadable();

    public static UavTimelineDetailsResponse create(boolean isActive, UUID uuid, DateTime seenFrom, DateTime seenTo, Double maxDistance, Double minDistance, Long durationSeconds, String durationHumanReadable) {
        return builder()
                .isActive(isActive)
                .uuid(uuid)
                .seenFrom(seenFrom)
                .seenTo(seenTo)
                .maxDistance(maxDistance)
                .minDistance(minDistance)
                .durationSeconds(durationSeconds)
                .durationHumanReadable(durationHumanReadable)
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

        public abstract Builder maxDistance(Double maxDistance);

        public abstract Builder minDistance(Double minDistance);

        public abstract Builder durationSeconds(Long durationSeconds);

        public abstract Builder durationHumanReadable(String durationHumanReadable);

        public abstract UavTimelineDetailsResponse build();
    }
}
