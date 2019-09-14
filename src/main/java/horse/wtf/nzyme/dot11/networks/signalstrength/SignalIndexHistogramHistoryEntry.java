package horse.wtf.nzyme.dot11.networks.signalstrength;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class SignalIndexHistogramHistoryEntry {

    public abstract String histogram();
    public abstract DateTime createdAt();

    public static SignalIndexHistogramHistoryEntry create(String histogram, DateTime createdAt) {
        return builder()
                .histogram(histogram)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SignalIndexHistogramHistoryEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder histogram(String histogram);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract SignalIndexHistogramHistoryEntry build();
    }

}
