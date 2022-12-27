package app.nzyme.core.dot11.networks.signalstrength;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class SignalIndexHistogramHistoryDBEntry {

    public abstract String histogram();
    public abstract DateTime createdAt();

    public static SignalIndexHistogramHistoryDBEntry create(String histogram, DateTime createdAt) {
        return builder()
                .histogram(histogram)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SignalIndexHistogramHistoryDBEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder histogram(String histogram);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract SignalIndexHistogramHistoryDBEntry build();
    }

}
