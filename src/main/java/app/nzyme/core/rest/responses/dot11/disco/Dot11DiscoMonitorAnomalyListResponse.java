package app.nzyme.core.rest.responses.dot11.disco;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class Dot11DiscoMonitorAnomalyListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("anomalies")
    public abstract List<Dot11DiscoMonitorAnomalyDetailsResponse> anomalies();

    public static Dot11DiscoMonitorAnomalyListResponse create(long total, List<Dot11DiscoMonitorAnomalyDetailsResponse> anomalies) {
        return builder()
                .total(total)
                .anomalies(anomalies)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11DiscoMonitorAnomalyListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder anomalies(List<Dot11DiscoMonitorAnomalyDetailsResponse> anomalies);

        public abstract Dot11DiscoMonitorAnomalyListResponse build();
    }
}
