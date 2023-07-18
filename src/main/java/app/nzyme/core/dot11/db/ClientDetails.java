package app.nzyme.core.dot11.db;

import app.nzyme.core.rest.responses.dot11.clients.ConnectedBSSID;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class ClientDetails {

    public abstract String mac();
    @Nullable
    public abstract String macOui();
    @Nullable
    public abstract ConnectedBSSID connectedBSSID();
    public abstract List<ConnectedBSSID> connectedBSSIDHistory();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();
    public abstract List<String> probeRequests();
    public abstract List<ClientActivityHistogramEntry> connectedFramesHistogram();
    public abstract List<ClientActivityHistogramEntry> disconnectedFramesHistogram();

    public static ClientDetails create(String mac, String macOui, ConnectedBSSID connectedBSSID, List<ConnectedBSSID> connectedBSSIDHistory, DateTime firstSeen, DateTime lastSeen, List<String> probeRequests, List<ClientActivityHistogramEntry> connectedFramesHistogram, List<ClientActivityHistogramEntry> disconnectedFramesHistogram) {
        return builder()
                .mac(mac)
                .macOui(macOui)
                .connectedBSSID(connectedBSSID)
                .connectedBSSIDHistory(connectedBSSIDHistory)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .probeRequests(probeRequests)
                .connectedFramesHistogram(connectedFramesHistogram)
                .disconnectedFramesHistogram(disconnectedFramesHistogram)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClientDetails.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mac(String mac);

        public abstract Builder macOui(String macOui);

        public abstract Builder connectedBSSID(ConnectedBSSID connectedBSSID);

        public abstract Builder connectedBSSIDHistory(List<ConnectedBSSID> connectedBSSIDHistory);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder probeRequests(List<String> probeRequests);

        public abstract Builder connectedFramesHistogram(List<ClientActivityHistogramEntry> connectedFramesHistogram);

        public abstract Builder disconnectedFramesHistogram(List<ClientActivityHistogramEntry> disconnectedFramesHistogram);

        public abstract ClientDetails build();
    }
}
