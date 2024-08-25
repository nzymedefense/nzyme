package app.nzyme.core.rest.responses.dot11.monitoring.probereq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class MonitoredProbeRequestDetailsResponse {

    @JsonProperty("id")
    public abstract UUID uuid();

    @JsonProperty("organization_id")
    public abstract UUID organizationId();

    @JsonProperty("tenant_id")
    public abstract UUID tenantId();

    @JsonProperty("ssid")
    public abstract String ssid();

    @Nullable
    @JsonProperty("notes")
    public abstract String notes();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static MonitoredProbeRequestDetailsResponse create(UUID uuid, UUID organizationId, UUID tenantId, String ssid, String notes, DateTime updatedAt, DateTime createdAt) {
        return builder()
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .ssid(ssid)
                .notes(notes)
                .updatedAt(updatedAt)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredProbeRequestDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder ssid(String ssid);

        public abstract Builder notes(String notes);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract MonitoredProbeRequestDetailsResponse build();
    }
}
