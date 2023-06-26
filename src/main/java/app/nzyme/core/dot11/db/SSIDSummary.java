package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class SSIDSummary {

    public abstract String ssid();
    public abstract List<String> securityProtocols();
    public abstract List<Boolean> isWps();
    public abstract float signalStrengthAverage();
    public abstract int frequency();
    public abstract long totalBytes();
    public abstract long totalFrames();
    public abstract DateTime lastSeen();

    public static SSIDSummary create(String ssid, List<String> securityProtocols, List<Boolean> isWps, float signalStrengthAverage, int frequency, long totalBytes, long totalFrames, DateTime lastSeen) {
        return builder()
                .ssid(ssid)
                .securityProtocols(securityProtocols)
                .isWps(isWps)
                .signalStrengthAverage(signalStrengthAverage)
                .frequency(frequency)
                .totalBytes(totalBytes)
                .totalFrames(totalFrames)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDSummary.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssid(String ssid);

        public abstract Builder securityProtocols(List<String> securityProtocols);

        public abstract Builder isWps(List<Boolean> isWps);

        public abstract Builder signalStrengthAverage(float signalStrengthAverage);

        public abstract Builder frequency(int frequency);

        public abstract Builder totalBytes(long totalBytes);

        public abstract Builder totalFrames(long totalFrames);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract SSIDSummary build();
    }
}
