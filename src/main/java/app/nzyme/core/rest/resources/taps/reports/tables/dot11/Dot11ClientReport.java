package app.nzyme.core.rest.resources.taps.reports.tables.dot11;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class Dot11ClientReport {

    public abstract Map<String, Long> probeRequestSSIDs();
    public abstract long wildcardProbeRequests();

    @JsonCreator
    public static Dot11ClientReport create(@JsonProperty("probe_request_ssids") Map<String, Long> probeRequestSSIDs,
                                           @JsonProperty("wildcard_probe_requests") long wildcardProbeRequests) {
        return builder()
                .probeRequestSSIDs(probeRequestSSIDs)
                .wildcardProbeRequests(wildcardProbeRequests)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11ClientReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder probeRequestSSIDs(Map<String, Long> probeRequestSSIDs);

        public abstract Builder wildcardProbeRequests(long wildcardProbeRequests);

        public abstract Dot11ClientReport build();
    }
}
