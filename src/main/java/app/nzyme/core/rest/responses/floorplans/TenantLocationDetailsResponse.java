package app.nzyme.core.rest.responses.floorplans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class TenantLocationDetailsResponse {

    @JsonProperty("id")
    public abstract UUID id();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("description")
    @Nullable
    public abstract String description();

    @JsonProperty("longitude")
    @Nullable
    public abstract Double longitude();

    @JsonProperty("latitude")
    @Nullable
    public abstract Double latitude();

    @JsonProperty("floor_count")
    public abstract long floorCount();

    @JsonProperty("tap_count")
    public abstract long tapCount();

    @JsonProperty("environmental_alert_eventing_enabled")
    public abstract boolean environmentalAlertEventingEnabled();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    public static TenantLocationDetailsResponse create(UUID id, String name, String description, Double longitude, Double latitude, long floorCount, long tapCount, boolean environmentalAlertEventingEnabled, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .name(name)
                .description(description)
                .longitude(longitude)
                .latitude(latitude)
                .floorCount(floorCount)
                .tapCount(tapCount)
                .environmentalAlertEventingEnabled(environmentalAlertEventingEnabled)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TenantLocationDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(UUID id);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder longitude(Double longitude);

        public abstract Builder latitude(Double latitude);

        public abstract Builder floorCount(long floorCount);

        public abstract Builder tapCount(long tapCount);

        public abstract Builder environmentalAlertEventingEnabled(boolean environmentalAlertEventingEnabled);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract TenantLocationDetailsResponse build();
    }
}
