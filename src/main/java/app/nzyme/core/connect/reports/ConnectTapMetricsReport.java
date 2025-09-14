package app.nzyme.core.connect.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class ConnectTapMetricsReport {

    @JsonProperty("gauges")
    public abstract Map<String, Double> gauges();

    @JsonProperty("timers")
    public abstract Map<String, ConnectTapTimerReport> timers();

    public static ConnectTapMetricsReport create(Map<String, Double> gauges, Map<String, ConnectTapTimerReport> timers) {
        return builder()
                .gauges(gauges)
                .timers(timers)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ConnectTapMetricsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder gauges(Map<String, Double> gauges);

        public abstract Builder timers(Map<String, ConnectTapTimerReport> timers);

        public abstract ConnectTapMetricsReport build();
    }
}
