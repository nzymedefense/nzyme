package app.nzyme.core.util;

import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class TenantCacheKey {

    public abstract UUID organization();
    public abstract UUID tenant();

    public static TenantCacheKey create(UUID organization, UUID tenant) {
        return builder()
                .organization(organization)
                .tenant(tenant)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TenantCacheKey.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder organization(UUID organization);

        public abstract Builder tenant(UUID tenant);

        public abstract TenantCacheKey build();
    }
}
