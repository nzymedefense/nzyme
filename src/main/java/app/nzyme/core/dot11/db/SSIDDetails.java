package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class SSIDDetails {

    public abstract String ssid();
    public abstract List<String> securityProtocols();
    public abstract List<String> fingerprints();
    public abstract List<Double> rates();
    public abstract List<Boolean> isWps();
    public abstract List<String> infrastructureTypes();
    public abstract List<String> securitySuites();
    public abstract List<String> accessPointClients();
    public abstract float signalStrengthAverage();
    public abstract long totalBytes();
    public abstract long totalFrames();
    public abstract DateTime lastSeen();

    public static SSIDDetails create(String ssid, List<String> securityProtocols, List<String> fingerprints, List<Double> rates, List<Boolean> isWps, List<String> infrastructureTypes, List<String> securitySuites, List<String> accessPointClients, float signalStrengthAverage, long totalBytes, long totalFrames, DateTime lastSeen) {
        return builder()
                .ssid(ssid)
                .securityProtocols(securityProtocols)
                .fingerprints(fingerprints)
                .rates(rates)
                .isWps(isWps)
                .infrastructureTypes(infrastructureTypes)
                .securitySuites(securitySuites)
                .accessPointClients(accessPointClients)
                .signalStrengthAverage(signalStrengthAverage)
                .totalBytes(totalBytes)
                .totalFrames(totalFrames)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDDetails.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssid(String ssid);

        public abstract Builder securityProtocols(List<String> securityProtocols);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Builder rates(List<Double> rates);

        public abstract Builder isWps(List<Boolean> isWps);

        public abstract Builder infrastructureTypes(List<String> infrastructureTypes);

        public abstract Builder securitySuites(List<String> securitySuites);

        public abstract Builder accessPointClients(List<String> accessPointClients);

        public abstract Builder signalStrengthAverage(float signalStrengthAverage);

        public abstract Builder totalBytes(long totalBytes);

        public abstract Builder totalFrames(long totalFrames);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract SSIDDetails build();
    }
}
