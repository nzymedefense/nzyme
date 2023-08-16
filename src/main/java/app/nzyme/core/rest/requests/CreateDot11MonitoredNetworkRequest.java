package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@AutoValue
public abstract class CreateDot11MonitoredNetworkRequest {

    @NotEmpty
    public abstract String ssid();

    @NotNull
    public abstract UUID organizationId();

    @NotNull
    public abstract UUID tenantId();

    @JsonCreator
    public static CreateDot11MonitoredNetworkRequest create(@NotEmpty @JsonProperty("ssid") String ssid, @NotNull @JsonProperty("organization_id") UUID organizationId, @NotNull @JsonProperty("tenant_id") UUID tenantId) {
        return builder()
                .ssid(ssid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateDot11MonitoredNetworkRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssid(@NotEmpty String ssid);

        public abstract Builder organizationId(@NotNull UUID organizationId);

        public abstract Builder tenantId(@NotNull UUID tenantId);

        public abstract CreateDot11MonitoredNetworkRequest build();
    }
}
