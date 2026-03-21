package app.nzyme.core.rest.responses.monitors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class MonitorListResponse {

    @JsonProperty("total")
    public abstract Long total();

    @JsonProperty("monitors")
    public abstract List<MonitorDetailsResponse> monitors();

    public static MonitorListResponse create(Long total, List<MonitorDetailsResponse> monitors) {
        return builder()
                .total(total)
                .monitors(monitors)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitorListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(Long total);

        public abstract Builder monitors(List<MonitorDetailsResponse> monitors);

        public abstract MonitorListResponse build();
    }
}
