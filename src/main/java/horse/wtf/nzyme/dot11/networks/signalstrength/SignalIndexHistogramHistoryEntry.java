package horse.wtf.nzyme.dot11.networks.signalstrength;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SignalIndexHistogramHistoryEntry {

    @JsonProperty
    public abstract long count();

    @JsonProperty
    public abstract double index();

    public static SignalIndexHistogramHistoryEntry create(long count, double index) {
        return builder()
                .count(count)
                .index(index)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SignalIndexHistogramHistoryEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder index(double index);

        public abstract SignalIndexHistogramHistoryEntry build();
    }
    
}
