package app.nzyme.core.context;

import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class MacAddressContextCacheKey {

    public abstract String macAddress();
    public abstract UUID organizationId();
    public abstract UUID tenantId();

    public static MacAddressContextCacheKey create(String macAddress, UUID organizationId, UUID tenantId) {
        return builder()
                .macAddress(macAddress)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MacAddressContextCacheKey.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder macAddress(String macAddress);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract MacAddressContextCacheKey build();
    }

}
