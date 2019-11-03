package horse.wtf.nzyme.dot11.networks.signalstrength.tracks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class Track {

    @JsonProperty
    public abstract String id();

    @JsonProperty
    public abstract DateTime start();

    @JsonProperty
    public abstract DateTime end();

    @JsonProperty("min_signal")
    public abstract int minSignal();

    @JsonProperty("max_signal")
    public abstract int maxSignal();

    public static Track create(String id, DateTime start, DateTime end, int minSignal, int maxSignal) {
        return builder()
                .id(id)
                .start(start)
                .end(end)
                .minSignal(minSignal)
                .maxSignal(maxSignal)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Track.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);

        public abstract Builder start(DateTime start);

        public abstract Builder end(DateTime end);

        public abstract Builder minSignal(int minSignal);

        public abstract Builder maxSignal(int maxSignal);

        public abstract Track build();
    }
}
