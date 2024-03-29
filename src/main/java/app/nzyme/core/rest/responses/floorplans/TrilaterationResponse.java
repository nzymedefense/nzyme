package app.nzyme.core.rest.responses.floorplans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class TrilaterationResponse {

    @JsonProperty("locations")
    public abstract Map<DateTime, TrilaterationLocationResponse> locations();

    @JsonProperty("outside_of_plan_boundaries_percentage")
    public abstract double outsideOfPlanBoundariesPercentage();

    @JsonProperty("is_outside_of_plan_boundaries")
    public abstract boolean isOutsideOfPlanBoundaries();

    @JsonProperty("outside_of_plan_tap_strengths")
    public abstract Map<Integer, Map<String, Integer>> outsideOfPlanBoundariesTapStrengths();

    @JsonProperty("plan")
    public abstract FloorPlanResponse plan();

    @JsonProperty("tenant_location")
    public abstract TenantLocationDetailsResponse tenantLocation();

    @JsonProperty("tenant_floor")
    public abstract TenantLocationFloorDetailsResponse tenantFloor();

    @JsonProperty("generated_at")
    public abstract DateTime generatedAt();

    @JsonProperty("target_description")
    public abstract String targetDescription();

    public static TrilaterationResponse create(Map<DateTime, TrilaterationLocationResponse> locations, double outsideOfPlanBoundariesPercentage, boolean isOutsideOfPlanBoundaries, Map<Integer, Map<String, Integer>> outsideOfPlanBoundariesTapStrengths, FloorPlanResponse plan, TenantLocationDetailsResponse tenantLocation, TenantLocationFloorDetailsResponse tenantFloor, DateTime generatedAt, String targetDescription) {
        return builder()
                .locations(locations)
                .outsideOfPlanBoundariesPercentage(outsideOfPlanBoundariesPercentage)
                .isOutsideOfPlanBoundaries(isOutsideOfPlanBoundaries)
                .outsideOfPlanBoundariesTapStrengths(outsideOfPlanBoundariesTapStrengths)
                .plan(plan)
                .tenantLocation(tenantLocation)
                .tenantFloor(tenantFloor)
                .generatedAt(generatedAt)
                .targetDescription(targetDescription)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TrilaterationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder locations(Map<DateTime, TrilaterationLocationResponse> locations);

        public abstract Builder outsideOfPlanBoundariesPercentage(double outsideOfPlanBoundariesPercentage);

        public abstract Builder isOutsideOfPlanBoundaries(boolean isOutsideOfPlanBoundaries);

        public abstract Builder outsideOfPlanBoundariesTapStrengths(Map<Integer, Map<String, Integer>> outsideOfPlanBoundariesTapStrengths);

        public abstract Builder plan(FloorPlanResponse plan);

        public abstract Builder tenantLocation(TenantLocationDetailsResponse tenantLocation);

        public abstract Builder tenantFloor(TenantLocationFloorDetailsResponse tenantFloor);

        public abstract Builder generatedAt(DateTime generatedAt);

        public abstract Builder targetDescription(String targetDescription);

        public abstract TrilaterationResponse build();
    }
}
