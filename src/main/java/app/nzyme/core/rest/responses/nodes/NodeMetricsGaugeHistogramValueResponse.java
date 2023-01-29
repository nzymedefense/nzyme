package app.nzyme.core.rest.responses.nodes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class NodeMetricsGaugeHistogramValueResponse {

    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    @JsonProperty("sum")
    public abstract Double sum();

    @JsonProperty("average")
    public abstract Double average();

    @JsonProperty("maximum")
    public abstract Double maximum();

    @JsonProperty("minimum")
    public abstract Double minimum();

    public static NodeMetricsGaugeHistogramValueResponse create(DateTime timestamp, Double sum, Double average, Double maximum, Double minimum) {
        return builder()
                .timestamp(timestamp)
                .sum(sum)
                .average(average)
                .maximum(maximum)
                .minimum(minimum)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_NodeMetricsGaugeHistogramValueResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder sum(Double sum);

        public abstract Builder average(Double average);

        public abstract Builder maximum(Double maximum);

        public abstract Builder minimum(Double minimum);

        public abstract NodeMetricsGaugeHistogramValueResponse build();
    }

}
