package app.nzyme.core.connect.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class ConnectNodeMetricsReport {

    @JsonProperty("gauges")
    public abstract Map<String, Double> gauges();

    @JsonProperty("timers")
    public abstract Map<String, ConnectNodeTimerReport> timers();

    public static ConnectNodeMetricsReport create(Map<String, Double> gauges, Map<String, ConnectNodeTimerReport> timers) {
        return builder()
                .gauges(gauges)
                .timers(timers)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectNodeMetricsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder gauges(Map<String, Double> gauges);

        public abstract Builder timers(Map<String, ConnectNodeTimerReport> timers);

        public abstract ConnectNodeMetricsReport build();
    }

}
