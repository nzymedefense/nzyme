package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class CreateMonitorRequest {

    @JsonProperty
    @NotEmpty
    public abstract String name();

    @JsonProperty
    @Nullable
    public abstract String description();

    @JsonProperty
    @Nullable
    public abstract List<String> taps();

    @JsonProperty
    @Min(0)
    public abstract Integer triggerCondition();

    @JsonProperty
    @Min(1)
    public abstract Integer interval();

    @NotEmpty
    public abstract String filters();

    @JsonProperty("organization_id")
    @NotNull
    public abstract UUID organizationId();

    @JsonProperty("tenant_id")
    @NotNull
    public abstract UUID tenantId();

    @JsonCreator
    public static CreateMonitorRequest create(@JsonProperty("name") String name,
                                              @JsonProperty("description") String description,
                                              @JsonProperty("taps") List<String> taps,
                                              @JsonProperty("trigger_condition") Integer triggerCondition,
                                              @JsonProperty("interval") Integer interval,
                                              @JsonProperty("filters") String filters,
                                              @JsonProperty("organization_id") UUID organizationId,
                                              @JsonProperty("tenant_id") UUID tenantId) {
        return builder()
                .name(name)
                .description(description)
                .taps(taps)
                .triggerCondition(triggerCondition)
                .interval(interval)
                .filters(filters)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateMonitorRequest.Builder();
    }


    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(@NotEmpty String name);

        public abstract Builder description(String description);

        public abstract Builder taps(@NotEmpty List<String> taps);

        public abstract Builder triggerCondition(@Min(0) Integer triggerCondition);

        public abstract Builder interval(@Min(1) Integer interval);

        public abstract Builder filters(@NotEmpty String filters);

        public abstract Builder organizationId(@NotNull UUID organizationId);

        public abstract Builder tenantId(@NotNull UUID tenantId);

        public abstract CreateMonitorRequest build();
    }
}
