package app.nzyme.core.distributed.database.metrics;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class GaugeHistogramBucket {
    public abstract DateTime bucket();

    public abstract Double sum();
    public abstract Double average();
    public abstract Double maximum();
    public abstract Double minimum();

    public static GaugeHistogramBucket create(DateTime bucket, Double sum, Double average, Double maximum, Double minimum) {
        return builder()
                .bucket(bucket)
                .sum(sum)
                .average(average)
                .maximum(maximum)
                .minimum(minimum)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GaugeHistogramBucket.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder sum(Double sum);

        public abstract Builder average(Double average);

        public abstract Builder maximum(Double maximum);

        public abstract Builder minimum(Double minimum);

        public abstract GaugeHistogramBucket build();
    }

}
