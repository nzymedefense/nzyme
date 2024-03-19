package app.nzyme.core.rest.responses.floorplans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class TenantLocationFloorDetailsResponse {

    @JsonProperty("id")
    public abstract UUID id();

    @JsonProperty("location_id")
    public abstract UUID locationId();

    @JsonProperty("number")
    public abstract long number();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("has_floor_plan")
    public abstract boolean hasFloorPlan();

    @JsonProperty("tap_count")
    public abstract long tapCount();

    @JsonProperty("tap_positions")
    public abstract List<TapPositionResponse> tapPositions();

    @JsonProperty("path_loss_exponent")
    public abstract float pathLossExponent();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    public static TenantLocationFloorDetailsResponse create(UUID id, UUID locationId, long number, String name, boolean hasFloorPlan, long tapCount, List<TapPositionResponse> tapPositions, float pathLossExponent, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .locationId(locationId)
                .number(number)
                .name(name)
                .hasFloorPlan(hasFloorPlan)
                .tapCount(tapCount)
                .tapPositions(tapPositions)
                .pathLossExponent(pathLossExponent)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TenantLocationFloorDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(UUID id);

        public abstract Builder locationId(UUID locationId);

        public abstract Builder number(long number);

        public abstract Builder name(String name);

        public abstract Builder hasFloorPlan(boolean hasFloorPlan);

        public abstract Builder tapCount(long tapCount);

        public abstract Builder tapPositions(List<TapPositionResponse> tapPositions);

        public abstract Builder pathLossExponent(float pathLossExponent);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract TenantLocationFloorDetailsResponse build();
    }
}
