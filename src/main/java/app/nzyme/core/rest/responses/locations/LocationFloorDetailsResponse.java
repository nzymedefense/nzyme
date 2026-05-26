package app.nzyme.core.rest.responses.locations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.UUID;

@AutoValue
public abstract class LocationFloorDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();
    @JsonProperty("number")
    public abstract long number();
    @JsonProperty("name")
    @Nullable
    public abstract String name();
    @JsonProperty("has_floor_plan")
    public abstract boolean hasFloorPlan();
    @JsonProperty("tap_count")
    public abstract int tapCount();

    public static LocationFloorDetailsResponse create(UUID uuid, long number, String name, boolean hasFloorPlan, int tapCount) {
        return builder()
                .uuid(uuid)
                .number(number)
                .name(name)
                .hasFloorPlan(hasFloorPlan)
                .tapCount(tapCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_LocationFloorDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder number(long number);

        public abstract Builder name(String name);

        public abstract Builder hasFloorPlan(boolean hasFloorPlan);

        public abstract Builder tapCount(int tapCount);

        public abstract LocationFloorDetailsResponse build();
    }
}
