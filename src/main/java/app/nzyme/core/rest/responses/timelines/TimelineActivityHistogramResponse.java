package app.nzyme.core.rest.responses.timelines;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class TimelineActivityHistogramResponse {

    @JsonProperty("from")
    public abstract DateTime from();
    @JsonProperty("to")
    public abstract DateTime to();
    @JsonProperty("bucket_type")
    public abstract String bucketType();
    @JsonProperty("totals_by_event_type")
    public abstract Map<String, Long> totalsByEventType();
    @JsonProperty("buckets")
    public abstract List<TimelineActivityHistogramBucketResponse> buckets();

    public static TimelineActivityHistogramResponse create(DateTime from, DateTime to, String bucketType, Map<String, Long> totalsByEventType, List<TimelineActivityHistogramBucketResponse> buckets) {
        return builder()
                .from(from)
                .to(to)
                .bucketType(bucketType)
                .totalsByEventType(totalsByEventType)
                .buckets(buckets)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimelineActivityHistogramResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder from(DateTime from);

        public abstract Builder to(DateTime to);

        public abstract Builder bucketType(String bucketType);

        public abstract Builder totalsByEventType(Map<String, Long> totalsByEventType);

        public abstract Builder buckets(List<TimelineActivityHistogramBucketResponse> buckets);

        public abstract TimelineActivityHistogramResponse build();
    }
}
