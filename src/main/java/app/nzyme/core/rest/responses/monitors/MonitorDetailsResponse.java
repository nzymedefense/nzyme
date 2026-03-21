package app.nzyme.core.rest.responses.monitors;

import app.nzyme.core.rest.responses.taps.TapHighLevelInformationDetailsResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class MonitorDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("organization_id")
    public abstract UUID organizationId();

    @JsonProperty("tenant_id")
    public abstract UUID tenantId();

    @JsonProperty("enabled")
    public abstract boolean enabled();

    @JsonProperty("type")
    public abstract String type();

    @JsonProperty("name")
    public abstract String name();

    @Nullable
    @JsonProperty("description")
    public abstract String description();

    @Nullable
    @JsonProperty("taps")
    public abstract List<TapHighLevelInformationDetailsResponse> taps();

    @JsonProperty("trigger_condition")
    public abstract int triggerCondition();

    @JsonProperty("interval")
    public abstract int interval();

    @JsonProperty("filters")
    public abstract String filters();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    public static MonitorDetailsResponse create(UUID uuid, UUID organizationId, UUID tenantId, boolean enabled, String type, String name, String description, List<TapHighLevelInformationDetailsResponse> taps, int triggerCondition, int interval, String filters, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .enabled(enabled)
                .type(type)
                .name(name)
                .description(description)
                .taps(taps)
                .triggerCondition(triggerCondition)
                .interval(interval)
                .filters(filters)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitorDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder enabled(boolean enabled);

        public abstract Builder type(String type);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder taps(List<TapHighLevelInformationDetailsResponse> taps);

        public abstract Builder triggerCondition(int triggerCondition);

        public abstract Builder interval(int interval);

        public abstract Builder filters(String filters);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract MonitorDetailsResponse build();
    }
}
