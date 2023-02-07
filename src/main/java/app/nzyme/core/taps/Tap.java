package app.nzyme.core.taps;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class Tap {

    public abstract String name();
    public abstract DateTime clock();
    public abstract TotalWithAverage processedBytes();
    public abstract Long memoryTotal();
    public abstract Long memoryFree();
    public abstract Long memoryUsed();
    public abstract Double cpuLoad();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();
    public abstract Long clockDriftMs();

    public static Tap create(String name, DateTime clock, TotalWithAverage processedBytes, Long memoryTotal, Long memoryFree, Long memoryUsed, Double cpuLoad, DateTime createdAt, DateTime updatedAt, Long clockDriftMs) {
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

        public abstract Builder memoryTotal(Long memoryTotal);

        public abstract Builder memoryFree(Long memoryFree);

        public abstract Builder memoryUsed(Long memoryUsed);

        public abstract Builder cpuLoad(Double cpuLoad);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder clockDriftMs(Long clockDriftMs);

        public abstract Tap build();
    }
}
