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

    public static TrilaterationResponse create(Map<DateTime, TrilaterationLocationResponse> locations, FloorPlanResponse plan, TenantLocationDetailsResponse tenantLocation, TenantLocationFloorDetailsResponse tenantFloor, DateTime generatedAt, String targetDescription) {
        return builder()
                .locations(locations)
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

        public abstract Builder plan(FloorPlanResponse plan);

        public abstract Builder tenantLocation(TenantLocationDetailsResponse tenantLocation);

        public abstract Builder tenantFloor(TenantLocationFloorDetailsResponse tenantFloor);

        public abstract Builder generatedAt(DateTime generatedAt);

        public abstract Builder targetDescription(String targetDescription);

        public abstract TrilaterationResponse build();
    }
}
