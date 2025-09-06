package app.nzyme.core.rest.resources.taps.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.Map;

@AutoValue
public abstract class SystemMetrics {

    public abstract Double cpuLoad();
    public abstract Map<Integer, Double> cpuCoresLoad();
    public abstract Long memoryTotal();
    public abstract Long memoryFree();

    @Nullable
    public abstract Double rpiTemperature();

    @JsonCreator
    public static SystemMetrics create(@JsonProperty("cpu_load") Double cpuLoad,
                                       @JsonProperty("cpu_cores_load") Map<Integer, Double> cpuCoresLoad,
                                       @JsonProperty("memory_total") Long memoryTotal,
                                       @JsonProperty("memory_free") Long memoryFree,
                                       @JsonProperty("rpi_temperature") Double rpiTemperature) {
        return builder()
                .cpuLoad(cpuLoad)
                .cpuCoresLoad(cpuCoresLoad)
                .memoryTotal(memoryTotal)
                .memoryFree(memoryFree)
                .rpiTemperature(rpiTemperature)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SystemMetrics.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder cpuLoad(Double cpuLoad);

        public abstract Builder cpuCoresLoad(Map<Integer, Double> cpuCoresLoad);

        public abstract Builder memoryTotal(Long memoryTotal);

        public abstract Builder memoryFree(Long memoryFree);

        public abstract Builder rpiTemperature(Double rpiTemperature);

        public abstract SystemMetrics build();
    }

}
