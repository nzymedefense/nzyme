package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class SSIDChannelDetails {

    public abstract String ssid();
    public abstract List<String> securityProtocols();
    public abstract List<Boolean> isWps();
    public abstract List<String> infrastructureTypes();
    public abstract float signalStrengthAverage();
    public abstract int frequency();
    public abstract long totalBytes();
    public abstract long totalFrames();
    public abstract DateTime lastSeen();

    public static SSIDChannelDetails create(String ssid, List<String> securityProtocols, List<Boolean> isWps, List<String> infrastructureTypes, float signalStrengthAverage, int frequency, long totalBytes, long totalFrames, DateTime lastSeen) {
        return builder()
                .ssid(ssid)
                .securityProtocols(securityProtocols)
                .isWps(isWps)
                .infrastructureTypes(infrastructureTypes)
                .signalStrengthAverage(signalStrengthAverage)
                .frequency(frequency)
                .totalBytes(totalBytes)
                .totalFrames(totalFrames)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDChannelDetails.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssid(String ssid);

        public abstract Builder securityProtocols(List<String> securityProtocols);

        public abstract Builder isWps(List<Boolean> isWps);

        public abstract Builder infrastructureTypes(List<String> infrastructureTypes);

        public abstract Builder signalStrengthAverage(float signalStrengthAverage);

        public abstract Builder frequency(int frequency);

        public abstract Builder totalBytes(long totalBytes);

        public abstract Builder totalFrames(long totalFrames);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract SSIDChannelDetails build();
    }
}
