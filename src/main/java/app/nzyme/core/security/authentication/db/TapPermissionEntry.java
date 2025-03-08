package app.nzyme.core.security.authentication.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class TapPermissionEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract UUID organizationId();
    public abstract UUID tenantId();

    public abstract String name();
    public abstract String description();

    @Nullable
    public abstract Double latitude();

    @Nullable
    public abstract Double longitude();

    public abstract String secret();

    @Nullable
    public abstract UUID locationId();

    @Nullable
    public abstract UUID floorId();

    @Nullable
    public abstract Integer floorLocationX();

    @Nullable
    public abstract Integer floorLocationY();

    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    @Nullable
    public abstract DateTime lastReport();

    public static TapPermissionEntry create(long id, UUID uuid, UUID organizationId, UUID tenantId, String name, String description, Double latitude, Double longitude, String secret, UUID locationId, UUID floorId, Integer floorLocationX, Integer floorLocationY, DateTime createdAt, DateTime updatedAt, DateTime lastReport) {
        return builder()
                .id(id)
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .latitude(latitude)
                .longitude(longitude)
                .secret(secret)
                .locationId(locationId)
                .floorId(floorId)
                .floorLocationX(floorLocationX)
                .floorLocationY(floorLocationY)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .lastReport(lastReport)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapPermissionEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder latitude(Double latitude);

        public abstract Builder longitude(Double longitude);

        public abstract Builder secret(String secret);

        public abstract Builder locationId(UUID locationId);

        public abstract Builder floorId(UUID floorId);

        public abstract Builder floorLocationX(Integer floorLocationX);

        public abstract Builder floorLocationY(Integer floorLocationY);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder lastReport(DateTime lastReport);

        public abstract TapPermissionEntry build();
    }
}
