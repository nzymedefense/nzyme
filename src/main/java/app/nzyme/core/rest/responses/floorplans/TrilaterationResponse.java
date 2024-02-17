package app.nzyme.core.rest.responses.floorplans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TrilaterationResponse {

    @JsonProperty("location")
    public abstract TrilaterationLocationResponse location();

    @JsonProperty("plan")
    public abstract FloorPlanResponse plan();

    @JsonProperty("tenant_location")
    public abstract TenantLocationDetailsResponse tenantLocation();

    @JsonProperty("tenant_floor")
    public abstract TenantLocationFloorDetailsResponse tenantFloor();

    public static TrilaterationResponse create(TrilaterationLocationResponse location, FloorPlanResponse plan, TenantLocationDetailsResponse tenantLocation, TenantLocationFloorDetailsResponse tenantFloor) {
        return builder()
                .location(location)
                .plan(plan)
                .tenantLocation(tenantLocation)
                .tenantFloor(tenantFloor)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TrilaterationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder location(TrilaterationLocationResponse location);

        public abstract Builder plan(FloorPlanResponse plan);

        public abstract Builder tenantLocation(TenantLocationDetailsResponse tenantLocation);

        public abstract Builder tenantFloor(TenantLocationFloorDetailsResponse tenantFloor);

        public abstract TrilaterationResponse build();
    }
}
