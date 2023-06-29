package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class BSSIDSummary {

    public abstract String bssid();
    public abstract float signalStrengthAverage();
    public abstract DateTime lastSeen();
    public abstract long hiddenSSIDFrames();
    public abstract List<String> ssids();
    public abstract List<String> securityProtocols();
    public abstract List<String> fingerprints();
    public abstract List<String> infrastructureTypes();

    public static BSSIDSummary create(String bssid, float signalStrengthAverage, DateTime lastSeen, long hiddenSSIDFrames, List<String> ssids, List<String> securityProtocols, List<String> fingerprints, List<String> infrastructureTypes) {
        return builder()
                .bssid(bssid)
                .signalStrengthAverage(signalStrengthAverage)
                .lastSeen(lastSeen)
                .hiddenSSIDFrames(hiddenSSIDFrames)
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

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder hiddenSSIDFrames(long hiddenSSIDFrames);

        public abstract Builder ssids(List<String> ssids);

        public abstract Builder securityProtocols(List<String> securityProtocols);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Builder infrastructureTypes(List<String> infrastructureTypes);

        public abstract BSSIDSummary build();
    }
}
