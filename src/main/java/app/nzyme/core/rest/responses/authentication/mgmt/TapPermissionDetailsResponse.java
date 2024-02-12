package app.nzyme.core.rest.responses.authentication.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class TapPermissionDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("organization_d")
    public abstract UUID organizationId();

    @JsonProperty("tenant_id")
    public abstract UUID tenantId();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("description")
    public abstract String description();

    @JsonProperty("secret")
    public abstract String secret();

    @JsonProperty("is_placed_on_map")
    public abstract boolean isPlacedOnMap();

    @Nullable
    @JsonProperty("location_id")
    public abstract UUID locationId();

    @Nullable
    @JsonProperty("floor_id")
    public abstract UUID floorId();

    @Nullable
    @JsonProperty("floor_location_x")
    public abstract Integer floorLocationX();

    @Nullable
    @JsonProperty("floor_location_y")
    public abstract Integer floorLocationY();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @Nullable
    @JsonProperty("last_report")
    public abstract DateTime lastReport();

    @JsonProperty("active")
    public abstract boolean active();

    public static TapPermissionDetailsResponse create(UUID uuid, UUID organizationId, UUID tenantId, String name, String description, String secret, boolean isPlacedOnMap, UUID locationId, UUID floorId, Integer floorLocationX, Integer floorLocationY, DateTime createdAt, DateTime updatedAt, DateTime lastReport, boolean active) {
        return builder()
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .secret(secret)
                .isPlacedOnMap(isPlacedOnMap)
                .locationId(locationId)
                .floorId(floorId)
                .floorLocationX(floorLocationX)
                .floorLocationY(floorLocationY)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .lastReport(lastReport)
                .active(active)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapPermissionDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder secret(String secret);

        public abstract Builder isPlacedOnMap(boolean isPlacedOnMap);

        public abstract Builder locationId(UUID locationId);

        public abstract Builder floorId(UUID floorId);

        public abstract Builder floorLocationX(Integer floorLocationX);

        public abstract Builder floorLocationY(Integer floorLocationY);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder lastReport(DateTime lastReport);

        public abstract Builder active(boolean active);

        public abstract TapPermissionDetailsResponse build();
    }
}
