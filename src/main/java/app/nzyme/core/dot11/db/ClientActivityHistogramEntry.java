package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ClientActivityHistogramEntry {

    public abstract DateTime bucket();
    public abstract long frames();

    public static ClientActivityHistogramEntry create(DateTime bucket, long frames) {
        return builder()
                .bucket(bucket)
                .frames(frames)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClientActivityHistogramEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder frames(long frames);

        public abstract ClientActivityHistogramEntry build();
    }
}
