package app.nzyme.core.rest.responses.dot11.monitoring.ssids;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class KnownNetworkDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("organization_id")
    public abstract UUID organizationId();

    @JsonProperty("tenant_id")
    public abstract UUID tenantId();

    @JsonProperty("ssid")
    public abstract String ssid();

    @JsonProperty("is_approved")
    public abstract boolean ispproved();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static KnownNetworkDetailsResponse create(UUID uuid, UUID organizationId, UUID tenantId, String ssid, boolean ispproved, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .uuid(uuid)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .ssid(ssid)
                .ispproved(ispproved)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_KnownNetworkDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder ssid(String ssid);

        public abstract Builder ispproved(boolean ispproved);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract KnownNetworkDetailsResponse build();
    }
}
