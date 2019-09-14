package horse.wtf.nzyme.dot11.networks.signalstrength;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SignalIndexHistogramHistory {

    @JsonProperty
    public abstract int count();

    @JsonProperty
    public abstract double index();

    public static SignalIndexHistogramHistory create(int count, double index) {
        return builder()
                .count(count)
                .index(index)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SignalIndexHistogramHistory.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(int count);

        public abstract Builder index(double index);

        public abstract SignalIndexHistogramHistory build();
    }

}
