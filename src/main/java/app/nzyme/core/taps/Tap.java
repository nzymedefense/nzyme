package app.nzyme.core.taps;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class Tap {

    public abstract String name();
    public abstract DateTime clock();
    public abstract TotalWithAverage processedBytes();
    public abstract long memoryTotal();
    public abstract long memoryFree();
    public abstract long memoryUsed();
    public abstract double cpuLoad();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();
    public abstract boolean deleted();
    public abstract long clockDriftMs();

    public static Tap create(String name, DateTime clock, TotalWithAverage processedBytes, long memoryTotal, long memoryFree, long memoryUsed, double cpuLoad, DateTime createdAt, DateTime updatedAt, boolean deleted, long clockDriftMs) {
        return builder()
                .name(name)
                .clock(clock)
                .processedBytes(processedBytes)
                .memoryTotal(memoryTotal)
                .memoryFree(memoryFree)
                .memoryUsed(memoryUsed)
                .cpuLoad(cpuLoad)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deleted(deleted)
                .clockDriftMs(clockDriftMs)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Tap.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder clock(DateTime clock);

        public abstract Builder processedBytes(TotalWithAverage processedBytes);

        public abstract Builder memoryTotal(long memoryTotal);

        public abstract Builder memoryFree(long memoryFree);

        public abstract Builder memoryUsed(long memoryUsed);

        public abstract Builder cpuLoad(double cpuLoad);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder deleted(boolean deleted);

        public abstract Builder clockDriftMs(long clockDriftMs);

        public abstract Tap build();
    }

}
