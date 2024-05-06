package app.nzyme.core.rest.responses.alerts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class DetectionAlertListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("total_active")
    public abstract long totalActive();

    @JsonProperty("alerts")
    public abstract List<DetectionAlertDetailsResponse> alerts();

    public static DetectionAlertListResponse create(long total, long totalActive, List<DetectionAlertDetailsResponse> alerts) {
        return builder()
                .total(total)
                .totalActive(totalActive)
                .alerts(alerts)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DetectionAlertListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder totalActive(long totalActive);

        public abstract Builder alerts(List<DetectionAlertDetailsResponse> alerts);

        public abstract DetectionAlertListResponse build();
    }
}
