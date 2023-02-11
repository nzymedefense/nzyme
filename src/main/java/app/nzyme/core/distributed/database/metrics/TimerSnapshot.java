package app.nzyme.core.distributed.database.metrics;

import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class TimerSnapshot {

    public abstract UUID nodeId();
    public abstract Long max();
    public abstract Long min();
    public abstract Long mean();
    public abstract Long p99();
    public abstract Long stddev();
    public abstract Long counter();

    public static TimerSnapshot create(UUID nodeId, Long max, Long min, Long mean, Long p99, Long stddev, Long counter) {
        return builder()
                .nodeId(nodeId)
                .max(max)
                .min(min)
                .mean(mean)
                .p99(p99)
                .stddev(stddev)
                .counter(counter)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimerSnapshot.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder nodeId(UUID nodeId);

        public abstract Builder max(Long max);

        public abstract Builder min(Long min);

        public abstract Builder mean(Long mean);

        public abstract Builder p99(Long p99);

        public abstract Builder stddev(Long stddev);

        public abstract Builder counter(Long counter);

        public abstract TimerSnapshot build();
    }

}
