package horse.wtf.nzyme.rest.resources.taps.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SystemMetrics {

    public abstract Double cpuLoad();
    public abstract Long memoryTotal();
    public abstract Long memoryFree();

    @JsonCreator
    public static SystemMetrics create(@JsonProperty("cpu_load") Double cpuLoad,
                                       @JsonProperty("memory_total") Long memoryTotal,
                                       @JsonProperty("memory_free") Long memoryFree) {
        return builder()
                .cpuLoad(cpuLoad)
                .memoryTotal(memoryTotal)
                .memoryFree(memoryFree)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SystemMetrics.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder cpuLoad(Double cpuLoad);

        public abstract Builder memoryTotal(Long memoryTotal);

        public abstract Builder memoryFree(Long memoryFree);

        public abstract SystemMetrics build();
    }

}
