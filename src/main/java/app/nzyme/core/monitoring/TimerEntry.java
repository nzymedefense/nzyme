package app.nzyme.core.monitoring;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class TimerEntry {

    public abstract long id();
    public abstract UUID nodeId();
    public abstract String name();
    public abstract long max();
    public abstract long min();
    public abstract long mean();
    public abstract long p99();
    public abstract long stddev();
    public abstract long counter();
    public abstract DateTime createdAt();

    public static TimerEntry create(long id, UUID nodeId, String name, long max, long min, long mean, long p99, long stddev, long counter, DateTime createdAt) {
        return builder()
                .id(id)
                .nodeId(nodeId)
                .name(name)
                .max(max)
                .min(min)
                .mean(mean)
                .p99(p99)
                .stddev(stddev)
                .counter(counter)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimerEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder nodeId(UUID nodeId);

        public abstract Builder name(String name);

        public abstract Builder max(long max);

        public abstract Builder min(long min);

        public abstract Builder mean(long mean);

        public abstract Builder p99(long p99);

        public abstract Builder stddev(long stddev);

        public abstract Builder counter(long counter);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract TimerEntry build();
    }
}
