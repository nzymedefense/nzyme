package app.nzyme.core.connect.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class ConnectMetricsReport {

    @JsonProperty("nodes")
    public abstract Map<UUID, ConnectNodeMetricsReport> nodes();

    @JsonProperty("taps")
    public abstract Map<UUID, ConnectTapMetricsReport> taps();

    public static ConnectMetricsReport create(Map<UUID, ConnectNodeMetricsReport> nodes, Map<UUID, ConnectTapMetricsReport> taps) {
        return builder()
                .nodes(nodes)
                .taps(taps)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectMetricsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder nodes(Map<UUID, ConnectNodeMetricsReport> nodes);

        public abstract Builder taps(Map<UUID, ConnectTapMetricsReport> taps);

        public abstract ConnectMetricsReport build();
    }
}
