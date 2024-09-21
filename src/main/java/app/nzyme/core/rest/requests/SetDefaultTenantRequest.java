package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.UUID;

@AutoValue
public abstract class SetDefaultTenantRequest {

    @Nullable
    public abstract UUID organizationId();

    @Nullable
    public abstract UUID tenantId();

    @JsonCreator
    public static SetDefaultTenantRequest create(@JsonProperty("organization_id") UUID organizationId,
                                                 @JsonProperty("tenant_id") UUID tenantId) {
        return builder()
                .organizationId(organizationId)
                .tenantId(tenantId)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SetDefaultTenantRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract SetDefaultTenantRequest build();
    }
}
