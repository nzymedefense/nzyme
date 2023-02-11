package app.nzyme.core.rest.responses.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class HealthResponse {

    @JsonProperty("indicators")
    public abstract Map<String, HealthIndicatorResponse> indicators();

    public static HealthResponse create(Map<String, HealthIndicatorResponse> indicators) {
        return builder()
                .indicators(indicators)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_HealthResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder indicators(Map<String, HealthIndicatorResponse> indicators);

        public abstract HealthResponse build();
    }

}
