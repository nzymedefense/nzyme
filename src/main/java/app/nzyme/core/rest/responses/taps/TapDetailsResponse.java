package app.nzyme.core.rest.responses.taps;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class TapDetailsResponse {

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("clock")
    public abstract DateTime clock();

    @JsonProperty("processed_bytes")
    public abstract TotalWithAverageResponse processedBytes();

    @JsonProperty("memory_total")
    public abstract long memoryTotal();

    @JsonProperty("memory_free")
    public abstract long memoryFree();

    @JsonProperty("memory_used")
    public abstract long memoryUsed();

    @JsonProperty("cpu_load")
    public abstract Double cpuLoad();

    @JsonProperty("active")
    public abstract boolean active();

    @JsonProperty("deleted")
    public abstract boolean deleted();

    @JsonProperty("clock_drift_ms")
    public abstract long clockDriftMs();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @JsonProperty("description")
    public abstract String description();

    @JsonProperty("buses")
    public abstract List<BusDetailsResponse> buses();

    @JsonProperty("captures")
    public abstract List<CaptureDetailsResponse> captures();

    public static TapDetailsResponse create(String name, DateTime clock, TotalWithAverageResponse processedBytes, long memoryTotal, long memoryFree, long memoryUsed, Double cpuLoad, boolean active, boolean deleted, long clockDriftMs, DateTime createdAt, DateTime updatedAt, String description, List<BusDetailsResponse> buses, List<CaptureDetailsResponse> captures) {
        return builder()
                .name(name)
                .clock(clock)
                .processedBytes(processedBytes)
                .memoryTotal(memoryTotal)
                .memoryFree(memoryFree)
                .memoryUsed(memoryUsed)
                .cpuLoad(cpuLoad)
                .active(active)
                .deleted(deleted)
                .clockDriftMs(clockDriftMs)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .description(description)
                .buses(buses)
                .captures(captures)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder clock(DateTime clock);

        public abstract Builder processedBytes(TotalWithAverageResponse processedBytes);

        public abstract Builder memoryTotal(long memoryTotal);

        public abstract Builder memoryFree(long memoryFree);

        public abstract Builder memoryUsed(long memoryUsed);

        public abstract Builder cpuLoad(Double cpuLoad);

        public abstract Builder active(boolean active);

        public abstract Builder deleted(boolean deleted);

        public abstract Builder clockDriftMs(long clockDriftMs);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder description(String description);

        public abstract Builder buses(List<BusDetailsResponse> buses);

        public abstract Builder captures(List<CaptureDetailsResponse> captures);

        public abstract TapDetailsResponse build();
    }

}
