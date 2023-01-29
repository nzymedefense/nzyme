package app.nzyme.core.rest.responses.nodes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.Map;

@AutoValue
public abstract class NodeMetricsGaugeHistogramResponse {

    @JsonProperty("values")
    public abstract Map<DateTime, NodeMetricsGaugeHistogramValueResponse> values();

    public static NodeMetricsGaugeHistogramResponse create(Map<DateTime, NodeMetricsGaugeHistogramValueResponse> values) {
        return builder()
                .values(values)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_NodeMetricsGaugeHistogramResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder values(Map<DateTime, NodeMetricsGaugeHistogramValueResponse> values);

        public abstract NodeMetricsGaugeHistogramResponse build();
    }

}
