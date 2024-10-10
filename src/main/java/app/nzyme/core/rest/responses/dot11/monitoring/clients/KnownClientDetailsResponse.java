package app.nzyme.core.rest.responses.dot11.monitoring.clients;

import app.nzyme.core.rest.responses.dot11.Dot11MacAddressResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class KnownClientDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("monitored_network_id")
    public abstract UUID monitoredNetworkId();

    @JsonProperty("mac")
    public abstract Dot11MacAddressResponse mac();

    @JsonProperty("is_approved")
    public abstract boolean isApproved();

    @JsonProperty("is_ignored")
    public abstract boolean isIgnored();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static KnownClientDetailsResponse create(UUID uuid, UUID monitoredNetworkId, Dot11MacAddressResponse mac, boolean isApproved, boolean isIgnored, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .uuid(uuid)
                .monitoredNetworkId(monitoredNetworkId)
                .mac(mac)
                .isApproved(isApproved)
                .isIgnored(isIgnored)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_KnownClientDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder monitoredNetworkId(UUID monitoredNetworkId);

        public abstract Builder mac(Dot11MacAddressResponse mac);

        public abstract Builder isApproved(boolean isApproved);

        public abstract Builder isIgnored(boolean isIgnored);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract KnownClientDetailsResponse build();
    }
}
