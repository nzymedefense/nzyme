package app.nzyme.core.rest.responses.dot11.clients;

import app.nzyme.core.rest.responses.dot11.Dot11MacAddressResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class DisconnectedClientDetailsResponse {

    @JsonProperty("mac")
    public abstract Dot11MacAddressResponse mac();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("probe_request_ssids")
    public abstract List<String> probeRequests();

    public static DisconnectedClientDetailsResponse create(Dot11MacAddressResponse mac, DateTime lastSeen, List<String> probeRequests) {
        return builder()
                .mac(mac)
                .lastSeen(lastSeen)
                .probeRequests(probeRequests)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DisconnectedClientDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(Dot11MacAddressResponse mac);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder probeRequests(List<String> probeRequests);

        public abstract DisconnectedClientDetailsResponse build();
    }
}
