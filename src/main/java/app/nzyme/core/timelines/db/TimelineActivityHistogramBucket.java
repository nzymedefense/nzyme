package app.nzyme.core.timelines.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.Map;

@AutoValue
public abstract class TimelineActivityHistogramBucket {

    public abstract DateTime bucket();
    public abstract long total();
    public abstract Map<String, Long> countsByEventType();

    public static TimelineActivityHistogramBucket create(DateTime bucket, long total, Map<String, Long> countsByEventType) {
        return builder()
                .bucket(bucket)
                .total(total)
                .countsByEventType(countsByEventType)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimelineActivityHistogramBucket.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder total(long total);

        public abstract Builder countsByEventType(Map<String, Long> countsByEventType);

        public abstract TimelineActivityHistogramBucket build();
    }
}
