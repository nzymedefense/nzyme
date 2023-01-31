package app.nzyme.core.rest.responses.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class CryptoNodeMetricsResponse {

    @JsonProperty("node_metrics")
    public abstract Map<String, CryptoMetricsResponse> nodeMetrics();

    @JsonProperty("cluster_metrics")
    public abstract CryptoMetricsResponse clusterMetrics();

    public static CryptoNodeMetricsResponse create(Map<String, CryptoMetricsResponse> nodeMetrics, CryptoMetricsResponse clusterMetrics) {
        return builder()
                .nodeMetrics(nodeMetrics)
                .clusterMetrics(clusterMetrics)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CryptoNodeMetricsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder nodeMetrics(Map<String, CryptoMetricsResponse> nodeMetrics);

        public abstract Builder clusterMetrics(CryptoMetricsResponse clusterMetrics);

        public abstract CryptoNodeMetricsResponse build();
    }
}
