package app.nzyme.core.dot11.tracks;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class PartialTrack {

    public abstract DateTime timestamp();
    public abstract int minSignal();
    public abstract int maxSignal();

    public int averageSignal() {
        return ((minSignal()+maxSignal())/2);
    }

    public static PartialTrack create(DateTime timestamp, int minSignal, int maxSignal) {
        return builder()
                .timestamp(timestamp)
                .minSignal(minSignal)
                .maxSignal(maxSignal)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PartialTrack.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder minSignal(int minSignal);

        public abstract Builder maxSignal(int maxSignal);

        public abstract PartialTrack build();
    }
}
