package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class BSSIDSummary {

    public abstract String bssid();
    public abstract float signalStrengthAverage();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();
    public abstract long hiddenSSIDFrames();
    public abstract long clientCount();
    public abstract List<String> ssids();
    public abstract List<String> securityProtocols();
    public abstract List<String> fingerprints();
    public abstract List<String> infrastructureTypes();

    public static BSSIDSummary create(String bssid, float signalStrengthAverage, DateTime firstSeen, DateTime lastSeen, long hiddenSSIDFrames, long clientCount, List<String> ssids, List<String> securityProtocols, List<String> fingerprints, List<String> infrastructureTypes) {
        return builder()
                .bssid(bssid)
                .signalStrengthAverage(signalStrengthAverage)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .hiddenSSIDFrames(hiddenSSIDFrames)
                .clientCount(clientCount)
                .ssids(ssids)
                .securityProtocols(securityProtocols)
                .fingerprints(fingerprints)
                .infrastructureTypes(infrastructureTypes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDSummary.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssid(String bssid);

        public abstract Builder signalStrengthAverage(float signalStrengthAverage);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder hiddenSSIDFrames(long hiddenSSIDFrames);

        public abstract Builder clientCount(long clientCount);

        public abstract Builder ssids(List<String> ssids);

        public abstract Builder securityProtocols(List<String> securityProtocols);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Builder infrastructureTypes(List<String> infrastructureTypes);

        public abstract BSSIDSummary build();
    }
}
