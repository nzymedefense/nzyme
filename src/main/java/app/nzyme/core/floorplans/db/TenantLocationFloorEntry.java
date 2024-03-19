package app.nzyme.core.floorplans.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class TenantLocationFloorEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract UUID locationId();
    public abstract long number();
    @Nullable
    public abstract String name();
    @Nullable
    public abstract byte[] plan();
    @Nullable
    public abstract Long planWidth();
    @Nullable
    public abstract Long planHeight();
    public abstract Float pathLossExponent();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static TenantLocationFloorEntry create(long id, UUID uuid, UUID locationId, long number, String name, byte[] plan, Long planWidth, Long planHeight, Float pathLossExponent, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .locationId(locationId)
                .number(number)
                .name(name)
                .plan(plan)
                .planWidth(planWidth)
                .planHeight(planHeight)
                .pathLossExponent(pathLossExponent)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TenantLocationFloorEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder locationId(UUID locationId);

        public abstract Builder number(long number);

        public abstract Builder name(String name);

        public abstract Builder plan(byte[] plan);

        public abstract Builder planWidth(Long planWidth);

        public abstract Builder planHeight(Long planHeight);

        public abstract Builder pathLossExponent(Float pathLossExponent);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract TenantLocationFloorEntry build();
    }
}
