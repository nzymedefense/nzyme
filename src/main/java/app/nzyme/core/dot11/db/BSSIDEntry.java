package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class BSSIDEntry {

    public abstract String bssid();
    public abstract float signalStrengthAverage();
    public abstract DateTime lastSeen();
    public abstract long hiddenSSIDFrames();

    public static BSSIDEntry create(String bssid, float signalStrengthAverage, DateTime lastSeen, long hiddenSSIDFrames) {
        return builder()
                .bssid(bssid)
                .signalStrengthAverage(signalStrengthAverage)
                .lastSeen(lastSeen)
                .hiddenSSIDFrames(hiddenSSIDFrames)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssid(String bssid);

        public abstract Builder signalStrengthAverage(float signalStrengthAverage);


        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder hiddenSSIDFrames(long hiddenSSIDFrames);

        public abstract BSSIDEntry build();
    }
}
