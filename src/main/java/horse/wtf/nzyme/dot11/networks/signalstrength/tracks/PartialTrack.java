package horse.wtf.nzyme.dot11.networks.signalstrength.tracks;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class PartialTrack {

    public abstract String id();
    public abstract DateTime timestamp();
    public abstract int minSignal();
    public abstract int maxSignal();

    public static PartialTrack create(String id, DateTime timestamp, int minSignal, int maxSignal) {
        return builder()
                .id(id)
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
        public abstract Builder id(String id);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder minSignal(int minSignal);

        public abstract Builder maxSignal(int maxSignal);

        public abstract PartialTrack build();
    }
}
