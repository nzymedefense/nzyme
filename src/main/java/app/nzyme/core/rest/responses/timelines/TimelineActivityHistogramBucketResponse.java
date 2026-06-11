package app.nzyme.core.rest.responses.timelines;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.Map;

@AutoValue
public abstract class TimelineActivityHistogramBucketResponse {

    @JsonProperty("bucket")
    public abstract DateTime bucket();
    @JsonProperty("total")
    public abstract long total();
    @JsonProperty("counts_by_event_type")
    public abstract Map<String, Long> countsByEventType();

    public static TimelineActivityHistogramBucketResponse create(DateTime bucket, long total, Map<String, Long> countsByEventType) {
        return builder()
                .bucket(bucket)
                .total(total)
                .countsByEventType(countsByEventType)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimelineActivityHistogramBucketResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder total(long total);

        public abstract Builder countsByEventType(Map<String, Long> countsByEventType);

        public abstract TimelineActivityHistogramBucketResponse build();
    }
}
