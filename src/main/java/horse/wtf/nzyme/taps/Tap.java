package horse.wtf.nzyme.taps;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class Tap {

    public abstract String name();
    public abstract DateTime localTime();
    public abstract TotalWithAverage processedBytes();
    public abstract Long memoryTotal();
    public abstract Long memoryFree();
    public abstract Long memoryUsed();
    public abstract Double cpuLoad();
    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    public static Tap create(String name, DateTime localTime, TotalWithAverage processedBytes, Long memoryTotal, Long memoryFree, Long memoryUsed, Double cpuLoad, DateTime createdAt, DateTime updatedAt) {
        return builder()
                .name(name)
                .localTime(localTime)
                .processedBytes(processedBytes)
                .memoryTotal(memoryTotal)
                .memoryFree(memoryFree)
                .memoryUsed(memoryUsed)
                .cpuLoad(cpuLoad)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Tap.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder localTime(DateTime localTime);

        public abstract Builder processedBytes(TotalWithAverage processedBytes);

        public abstract Builder memoryTotal(Long memoryTotal);

        public abstract Builder memoryFree(Long memoryFree);

        public abstract Builder memoryUsed(Long memoryUsed);

        public abstract Builder cpuLoad(Double cpuLoad);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Tap build();
    }
}
