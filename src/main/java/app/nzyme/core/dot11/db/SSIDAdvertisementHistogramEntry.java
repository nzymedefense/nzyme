package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class SSIDAdvertisementHistogramEntry {

    public abstract long beacons();
    public abstract long probeResponses();
    public abstract DateTime bucket();

    public static SSIDAdvertisementHistogramEntry create(long beacons, long probeResponses, DateTime bucket) {
        return builder()
                .beacons(beacons)
                .probeResponses(probeResponses)
                .bucket(bucket)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDAdvertisementHistogramEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder beacons(long beacons);

        public abstract Builder probeResponses(long probeResponses);

        public abstract Builder bucket(DateTime bucket);

        public abstract SSIDAdvertisementHistogramEntry build();
    }
}
