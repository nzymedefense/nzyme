package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ClientHistogramEntry {

    public abstract DateTime bucket();
    public abstract long clientCount();

    public static ClientHistogramEntry create(DateTime bucket, long clientCount) {
        return builder()
                .bucket(bucket)
                .clientCount(clientCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClientHistogramEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder clientCount(long clientCount);

        public abstract ClientHistogramEntry build();
    }
}
