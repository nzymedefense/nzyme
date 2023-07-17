package app.nzyme.core.rest.responses.dot11.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class ClientDetailsResponse {

    @JsonProperty("mac")
    public abstract String mac();

    @JsonProperty("mac_oui")
    @Nullable
    public abstract String macOui();

    @JsonProperty("connected_bssids")
    public abstract List<ConnectedBSSID> connectedBSSIDs();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("probe_requests")
    public abstract List<String> probeRequests();

    public static ClientDetailsResponse create(String mac, String macOui, List<ConnectedBSSID> connectedBSSIDs, DateTime lastSeen, List<String> probeRequests) {
        return builder()
                .mac(mac)
                .macOui(macOui)
                .connectedBSSIDs(connectedBSSIDs)
                .lastSeen(lastSeen)
                .probeRequests(probeRequests)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClientDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(String mac);

        public abstract Builder macOui(String macOui);

        public abstract Builder connectedBSSIDs(List<ConnectedBSSID> connectedBSSIDs);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder probeRequests(List<String> probeRequests);

        public abstract ClientDetailsResponse build();
    }
}
