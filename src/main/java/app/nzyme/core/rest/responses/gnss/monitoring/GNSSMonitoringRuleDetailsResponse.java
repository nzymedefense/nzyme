package app.nzyme.core.rest.responses.gnss.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class GNSSMonitoringRuleDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();
    @JsonProperty("organization_id")
    public abstract UUID organizationId();
    @JsonProperty("tenant_id")
    public abstract UUID tenantId();
    @JsonProperty("name")
    public abstract String name();
    @Nullable
    @JsonProperty("description")
    public abstract String description();
    @JsonProperty("conditions_count")
    public abstract int conditionsCount();
    @JsonProperty("conditions")
    public abstract Map<String, List<Object>> conditions();
    @JsonProperty("taps")
    @Nullable
    public abstract List<UUID> taps();
    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();
    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static GNSSMonitoringRuleDetailsResponse create(UUID uuid, UUID organizationId, UUID tenantId, String name, String description, int conditionsCount, Map<String, List<Object>> conditions, List<UUID> taps, DateTime updatedAt, DateTime createdAt) {
        return builder()
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .conditionsCount(conditionsCount)
                .conditions(conditions)
                .taps(taps)
                .updatedAt(updatedAt)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSMonitoringRuleDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder conditionsCount(int conditionsCount);

        public abstract Builder conditions(Map<String, List<Object>> conditions);

        public abstract Builder taps(List<UUID> taps);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract GNSSMonitoringRuleDetailsResponse build();
    }
}
