package horse.wtf.nzyme.rest.responses.metrics;

import com.codahale.metrics.Snapshot;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class HistogramResponse {

    @JsonProperty("median")
    public abstract double median();

    @JsonProperty("percentile_75")
    public abstract double p75();

    @JsonProperty("percentile_95")
    public abstract double p95();

    @JsonProperty("percentile_98")
    public abstract double p98();

    @JsonProperty("percentile_99")
    public abstract double p99();

    @JsonProperty("percentile_999")
    public abstract double p999();

    @JsonProperty("max")
    public abstract long max();

    @JsonProperty("mean")
    public abstract double mean();

    @JsonProperty("min")
    public abstract long min();

    @JsonProperty("stddev")
    public abstract double stddev();

    public static HistogramResponse fromSnapshot(Snapshot s) {
        return HistogramResponse.create(
                s.getMedian(),
                s.get75thPercentile(),
                s.get95thPercentile(),
                s.get98thPercentile(),
                s.get99thPercentile(),
                s.get999thPercentile(),
                s.getMax(),
                s.getMean(),
                s.getMin(),
                s.getStdDev()
        );
    }

    public static HistogramResponse create(double median, double p75, double p95, double p98, double p99, double p999, long max, double mean, long min, double stddev) {
        return builder()
                .median(median)
                .p75(p75)
                .p95(p95)
                .p98(p98)
                .p99(p99)
                .p999(p999)
                .max(max)
                .mean(mean)
                .min(min)
                .stddev(stddev)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_HistogramResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder median(double median);

        public abstract Builder p75(double p75);

        public abstract Builder p95(double p95);

        public abstract Builder p98(double p98);

        public abstract Builder p99(double p99);

        public abstract Builder p999(double p999);

        public abstract Builder max(long max);

        public abstract Builder mean(double mean);

        public abstract Builder min(long min);

        public abstract Builder stddev(double stddev);

        public abstract HistogramResponse build();
    }

}
