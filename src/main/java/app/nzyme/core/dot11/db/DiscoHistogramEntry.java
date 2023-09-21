package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class DiscoHistogramEntry {

    public abstract DateTime bucket();
    public abstract long frameCount();

    public static DiscoHistogramEntry create(DateTime bucket, long frameCount) {
        return builder()
                .bucket(bucket)
                .frameCount(frameCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DiscoHistogramEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder frameCount(long frameCount);

        public abstract DiscoHistogramEntry build();
    }
}
