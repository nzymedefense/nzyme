package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class DisconnectedClientDetails {

    public abstract String clientMac();
    public abstract DateTime lastSeen();
    public abstract List<String> probeRequests();
    public abstract Double signalStrength();

    public static DisconnectedClientDetails create(String clientMac, DateTime lastSeen, List<String> probeRequests, Double signalStrength) {
        return builder()
                .clientMac(clientMac)
                .lastSeen(lastSeen)
                .probeRequests(probeRequests)
                .signalStrength(signalStrength)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DisconnectedClientDetails.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder clientMac(String clientMac);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder probeRequests(List<String> probeRequests);

        public abstract Builder signalStrength(Double signalStrength);

        public abstract DisconnectedClientDetails build();
    }
}
