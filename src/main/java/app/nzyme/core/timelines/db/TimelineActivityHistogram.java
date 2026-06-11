package app.nzyme.core.timelines.db;

import app.nzyme.core.util.Bucketing;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class TimelineActivityHistogram {

    public abstract DateTime from();
    public abstract DateTime to();
    public abstract Bucketing.Type bucketType();
    public abstract long totalEvents();
    public abstract Map<String, Long> totalsByEventType();
    public abstract List<TimelineActivityHistogramBucket> buckets();

    public static TimelineActivityHistogram create(DateTime from, DateTime to, Bucketing.Type bucketType, long totalEvents, Map<String, Long> totalsByEventType, List<TimelineActivityHistogramBucket> buckets) {
        return builder()
                .from(from)
                .to(to)
                .bucketType(bucketType)
                .totalEvents(totalEvents)
                .totalsByEventType(totalsByEventType)
                .buckets(buckets)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimelineActivityHistogram.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder from(DateTime from);

        public abstract Builder to(DateTime to);

        public abstract Builder bucketType(Bucketing.Type bucketType);

        public abstract Builder totalEvents(long totalEvents);

        public abstract Builder totalsByEventType(Map<String, Long> totalsByEventType);

        public abstract Builder buckets(List<TimelineActivityHistogramBucket> buckets);

        public abstract TimelineActivityHistogram build();
    }
}
