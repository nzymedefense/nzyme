package app.nzyme.core.connect;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ConnectThroughputReport {

    @JsonProperty("bucket")
    public abstract DateTime bucket();

    @JsonProperty("average")
    public abstract Double average();

    @JsonProperty("maximum")
    public abstract Double maximum();

    @JsonProperty("minimum")
    public abstract Double minimum();

    public static ConnectThroughputReport create(DateTime bucket, Double average, Double maximum, Double minimum) {
        return builder()
                .bucket(bucket)
                .average(average)
                .maximum(maximum)
                .minimum(minimum)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectThroughputReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder average(Double average);

        public abstract Builder maximum(Double maximum);

        public abstract Builder minimum(Double minimum);

        public abstract ConnectThroughputReport build();
    }
}
