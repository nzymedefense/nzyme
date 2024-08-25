package app.nzyme.core.rest.responses.dot11.monitoring.probereq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class MonitoredProbeRequestListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("ssids")
    public abstract List<MonitoredProbeRequestDetailsResponse> ssids();

    public static MonitoredProbeRequestListResponse create(long total, List<MonitoredProbeRequestDetailsResponse> ssids) {
        return builder()
                .total(total)
                .ssids(ssids)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredProbeRequestListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder ssids(List<MonitoredProbeRequestDetailsResponse> ssids);

        public abstract MonitoredProbeRequestListResponse build();
    }
}
