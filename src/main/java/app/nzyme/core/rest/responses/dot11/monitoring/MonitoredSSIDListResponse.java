package app.nzyme.core.rest.responses.dot11.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class MonitoredSSIDListResponse {

    @JsonProperty("ssids")
    public abstract List<MonitoredSSIDDetailsResponse> ssids();

    public static MonitoredSSIDListResponse create(List<MonitoredSSIDDetailsResponse> ssids) {
        return builder()
                .ssids(ssids)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredSSIDListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssids(List<MonitoredSSIDDetailsResponse> ssids);

        public abstract MonitoredSSIDListResponse build();
    }
}
