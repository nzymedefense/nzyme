package app.nzyme.core.monitors;

import app.nzyme.core.monitoring.TimerEntryAverage;
import app.nzyme.core.monitors.db.MonitorEntry;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class MonitorPerformanceEntry {

    public abstract MonitorEntry monitor();

    @Nullable
    public abstract TimerEntryAverage executionTimer();

    public static MonitorPerformanceEntry create(MonitorEntry monitor, TimerEntryAverage executionTimer) {
        return builder()
                .monitor(monitor)
                .executionTimer(executionTimer)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitorPerformanceEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder monitor(MonitorEntry monitor);

        public abstract Builder executionTimer(TimerEntryAverage executionTimer);

        public abstract MonitorPerformanceEntry build();
    }
}
