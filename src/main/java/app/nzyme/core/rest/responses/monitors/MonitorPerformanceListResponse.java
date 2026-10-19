package app.nzyme.core.rest.responses.monitors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class MonitorPerformanceListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("monitors")
    public abstract List<MonitorPerformanceDetailsResponse> monitors();

    public static MonitorPerformanceListResponse create(long total, List<MonitorPerformanceDetailsResponse> monitors) {
        return builder()
                .total(total)
                .monitors(monitors)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitorPerformanceListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder monitors(List<MonitorPerformanceDetailsResponse> monitors);

        public abstract MonitorPerformanceListResponse build();
    }
}
