package app.nzyme.core.dot11.db;

import app.nzyme.core.rest.responses.dot11.clients.ConnectedBSSID;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class ClientDetails {

    public abstract String mac();
    @Nullable
    public abstract String macOui();
    public abstract List<ConnectedBSSID> connectedBSSIDs();
    public abstract DateTime lastSeen();
    public abstract List<String> probeRequests();

    public static ClientDetails create(String mac, String macOui, List<ConnectedBSSID> connectedBSSIDs, DateTime lastSeen, List<String> probeRequests) {
        return builder()
                .mac(mac)
                .macOui(macOui)
                .connectedBSSIDs(connectedBSSIDs)
                .lastSeen(lastSeen)
                .probeRequests(probeRequests)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClientDetails.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(String mac);

        public abstract Builder macOui(String macOui);

        public abstract Builder connectedBSSIDs(List<ConnectedBSSID> connectedBSSIDs);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder probeRequests(List<String> probeRequests);

        public abstract ClientDetails build();
    }
}
