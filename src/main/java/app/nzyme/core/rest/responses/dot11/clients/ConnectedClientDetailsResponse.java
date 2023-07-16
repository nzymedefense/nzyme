package app.nzyme.core.rest.responses.dot11.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class ConnectedClientDetailsResponse {

    @JsonProperty("mac")
    public abstract String clientMac();

    @JsonProperty("oui")
    @Nullable
    public abstract String clientOui();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("connected_bssid")
    public abstract String connectedBssid();

    @JsonProperty("connected_bssid_oui")
    @Nullable
    public abstract String connectedBssidOui();

    @JsonProperty("probe_request_ssids")
    public abstract List<String> probeRequests();

    @JsonProperty("bssid_history")
    public abstract List<ConnectedBSSID> bssidHistory();

    public static ConnectedClientDetailsResponse create(String clientMac, String clientOui, DateTime lastSeen, String connectedBssid, String connectedBssidOui, List<String> probeRequests, List<ConnectedBSSID> bssidHistory) {
        return builder()
                .clientMac(clientMac)
                .clientOui(clientOui)
                .lastSeen(lastSeen)
                .connectedBssid(connectedBssid)
                .connectedBssidOui(connectedBssidOui)
                .probeRequests(probeRequests)
                .bssidHistory(bssidHistory)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectedClientDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder clientMac(String clientMac);

        public abstract Builder clientOui(String clientOui);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder connectedBssid(String connectedBssid);

        public abstract Builder connectedBssidOui(String connectedBssidOui);

        public abstract Builder probeRequests(List<String> probeRequests);

        public abstract Builder bssidHistory(List<ConnectedBSSID> bssidHistory);

        public abstract ConnectedClientDetailsResponse build();
    }
}
