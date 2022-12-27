package app.nzyme.core.rest.responses.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class MonitoringSummaryResponse {

    @JsonProperty("exporters")
    public abstract Map<String, Boolean> exporters();

    public static MonitoringSummaryResponse create(Map<String, Boolean> exporters) {
        return builder()
                .exporters(exporters)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoringSummaryResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder exporters(Map<String, Boolean> exporters);

        public abstract MonitoringSummaryResponse build();
    }

}
