package app.nzyme.core.rest.responses.alerts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class DetectionAlertDetailsResponse {

    @JsonProperty("id")
    public abstract UUID id();

    @Nullable
    @JsonProperty("dot11_monitored_network_id")
    public abstract UUID dot11MonitoredNetworkId();

    @Nullable
    @JsonProperty("tap_id")
    public abstract UUID tapId();

    @JsonProperty("detection_type")
    public abstract String detectionType();

    @JsonProperty("subsystem")
    public abstract String subsystem();

    @JsonProperty("details")
    public abstract String details();

    @JsonProperty("attributes")
    public abstract Map<String, String> attributes();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("is_active")
    public abstract boolean isActive();

    @Nullable
    @JsonProperty("organization_id")
    public abstract UUID organizationId();

    @Nullable
    @JsonProperty("tenant_id")
    public abstract UUID tenantId();

    public static DetectionAlertDetailsResponse create(UUID id, UUID dot11MonitoredNetworkId, UUID tapId, String detectionType, String subsystem, String details, Map<String, String> attributes, DateTime createdAt, DateTime lastSeen, boolean isActive, UUID organizationId, UUID tenantId) {
        return builder()
                .id(id)
                .dot11MonitoredNetworkId(dot11MonitoredNetworkId)
                .tapId(tapId)
                .detectionType(detectionType)
                .subsystem(subsystem)
                .details(details)
                .attributes(attributes)
                .createdAt(createdAt)
                .lastSeen(lastSeen)
                .isActive(isActive)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DetectionAlertDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(UUID id);

        public abstract Builder dot11MonitoredNetworkId(UUID dot11MonitoredNetworkId);

        public abstract Builder tapId(UUID tapId);

        public abstract Builder detectionType(String detectionType);

        public abstract Builder subsystem(String subsystem);

        public abstract Builder details(String details);

        public abstract Builder attributes(Map<String, String> attributes);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder isActive(boolean isActive);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract DetectionAlertDetailsResponse build();
    }
}
