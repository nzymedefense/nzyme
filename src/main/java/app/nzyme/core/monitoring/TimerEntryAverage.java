package app.nzyme.core.monitoring;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TimerEntryAverage {

    public abstract String name();
    public abstract double max();
    public abstract double min();
    public abstract double mean();
    public abstract double p99();
    public abstract double stddev();
    public abstract double counter();

    public static TimerEntryAverage create(String name, double max, double min, double mean, double p99, double stddev, double counter) {
        return builder()
                .name(name)
                .max(max)
                .min(min)
                .mean(mean)
                .p99(p99)
                .stddev(stddev)
                .counter(counter)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimerEntryAverage.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder max(double max);

        public abstract Builder min(double min);

        public abstract Builder mean(double mean);

        public abstract Builder p99(double p99);

        public abstract Builder stddev(double stddev);

        public abstract Builder counter(double counter);

        public abstract TimerEntryAverage build();
    }
}
