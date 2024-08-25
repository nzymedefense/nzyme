package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@AutoValue
public abstract class CreateMonitoredProbeRequestRequest {

    @NotNull
    public abstract UUID organizationId();

    @NotNull
    public abstract UUID tenantId();

    @NotBlank
    public abstract String ssid();

    @Nullable
    public abstract String notes();

    @JsonCreator
    public static CreateMonitoredProbeRequestRequest create(@JsonProperty("organization_id") UUID organizationId,
                                                            @JsonProperty("tenant_id") UUID tenantId,
                                                            @JsonProperty("ssid") String ssid,
                                                            @JsonProperty("notes") String notes) {
        return builder()
                .organizationId(organizationId)
                .tenantId(tenantId)
                .ssid(ssid)
                .notes(notes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateMonitoredProbeRequestRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder organizationId(@NotNull UUID organizationId);

        public abstract Builder tenantId(@NotNull UUID tenantId);

        public abstract Builder ssid(@NotBlank String ssid);

        public abstract Builder notes(String notes);

        public abstract CreateMonitoredProbeRequestRequest build();
    }
}
