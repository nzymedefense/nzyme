package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ChannelHistogramEntry {

    public abstract DateTime bucket();
    public abstract int signalStrength();
    public abstract long frameCount();

    public static ChannelHistogramEntry create(DateTime bucket, int signalStrength, long frameCount) {
        return builder()
                .bucket(bucket)
                .signalStrength(signalStrength)
                .frameCount(frameCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ChannelHistogramEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder signalStrength(int signalStrength);

        public abstract Builder frameCount(long frameCount);

        public abstract ChannelHistogramEntry build();
    }
}
