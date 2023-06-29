package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class BSSIDAndSSIDCountHistogramEntry {

    public abstract long bssidCount();
    public abstract long ssidCount();
    public abstract DateTime bucket();

    public static BSSIDAndSSIDCountHistogramEntry create(long bssidCount, long ssidCount, DateTime bucket) {
        return builder()
                .bssidCount(bssidCount)
                .ssidCount(ssidCount)
                .bucket(bucket)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDAndSSIDCountHistogramEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssidCount(long bssidCount);

        public abstract Builder ssidCount(long ssidCount);

        public abstract Builder bucket(DateTime bucket);

        public abstract BSSIDAndSSIDCountHistogramEntry build();
    }
}
