package app.nzyme.core.rest.responses.dot11.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class DisconnectedClientDetailsResponse {

    @JsonProperty("mac")
    public abstract String clientMac();

    @JsonProperty("oui")
    @Nullable
    public abstract String clientOui();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("probe_request_ssids")
    public abstract List<String> probeRequests();

    @JsonProperty("bssid_history")
    public abstract List<ConnectedBSSID> bssidHistory();

    public static DisconnectedClientDetailsResponse create(String clientMac, String clientOui, DateTime lastSeen, List<String> probeRequests, List<ConnectedBSSID> bssidHistory) {
        return builder()
                .clientMac(clientMac)
                .clientOui(clientOui)
                .lastSeen(lastSeen)
                .probeRequests(probeRequests)
                .bssidHistory(bssidHistory)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DisconnectedClientDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder clientMac(String clientMac);

        public abstract Builder clientOui(String clientOui);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder probeRequests(List<String> probeRequests);

        public abstract Builder bssidHistory(List<ConnectedBSSID> bssidHistory);

        public abstract DisconnectedClientDetailsResponse build();
    }
}
