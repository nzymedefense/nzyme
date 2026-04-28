package app.nzyme.core.dot11.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class BSSIDSummary {

    @JsonProperty("bssid")
    public abstract String bssid();
    @JsonProperty("signal_strength_average")
    public abstract float signalStrengthAverage();
    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();
    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();
    @JsonProperty("hidden_ssid_frames")
    public abstract long hiddenSSIDFrames();
    @JsonProperty("client_count")
    public abstract long clientCount();
    @JsonProperty("ssids")
    public abstract List<String> ssids();
    @JsonProperty("security_protocols")
    public abstract List<String> securityProtocols();
    @JsonProperty("fingerprints")
    public abstract List<String> fingerprints();
    @JsonProperty("infrastructure_types")
    public abstract List<String> infrastructureTypes();
    @JsonProperty("frequencies")
    public abstract List<Integer> frequencies();

    public static BSSIDSummary create(String bssid, float signalStrengthAverage, DateTime firstSeen, DateTime lastSeen, long hiddenSSIDFrames, long clientCount, List<String> ssids, List<String> securityProtocols, List<String> fingerprints, List<String> infrastructureTypes, List<Integer> frequencies) {
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
                .frequencies(frequencies)
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

        public abstract Builder frequencies(List<Integer> frequencies);

        public abstract BSSIDSummary build();
    }
}
