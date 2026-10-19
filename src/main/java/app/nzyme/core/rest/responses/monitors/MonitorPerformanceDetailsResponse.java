package app.nzyme.core.rest.responses.monitors;

import app.nzyme.core.rest.responses.metrics.TimerResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class MonitorPerformanceDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("type")
    public abstract String type();

    @JsonProperty("enabled")
    public abstract boolean enabled();

    @JsonProperty("alerted")
    public abstract boolean alerted();

    @JsonProperty("status")
    public abstract String status();

    @JsonProperty("interval")
    public abstract int interval();

    @JsonProperty("lookback")
    public abstract int lookback();

    @Nullable
    @JsonProperty("last_run")
    public abstract DateTime lastRun();

    @Nullable
    @JsonProperty("last_event")
    public abstract DateTime lastEvent();

    @Nullable
    @Schema(description = "Execution timing of the monitor in microseconds, combined over all nodes that "
            + "reported a timer in the last two minutes. The call count is cumulative since those nodes started. "
            + "Null if no node has executed the monitor since it started.")
    @JsonProperty("execution_timer")
    public abstract TimerResponse executionTimer();

    public static MonitorPerformanceDetailsResponse create(UUID uuid, String name, String type, boolean enabled, boolean alerted, String status, int interval, int lookback, DateTime lastRun, DateTime lastEvent, TimerResponse executionTimer) {
        return builder()
                .uuid(uuid)
                .name(name)
                .type(type)
                .enabled(enabled)
                .alerted(alerted)
                .status(status)
                .interval(interval)
                .lookback(lookback)
                .lastRun(lastRun)
                .lastEvent(lastEvent)
                .executionTimer(executionTimer)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitorPerformanceDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder name(String name);

        public abstract Builder type(String type);

        public abstract Builder enabled(boolean enabled);

        public abstract Builder alerted(boolean alerted);

        public abstract Builder status(String status);

        public abstract Builder interval(int interval);

        public abstract Builder lookback(int lookback);

        public abstract Builder lastRun(DateTime lastRun);

        public abstract Builder lastEvent(DateTime lastEvent);

        public abstract Builder executionTimer(TimerResponse executionTimer);

        public abstract MonitorPerformanceDetailsResponse build();
    }
}
