package app.nzyme.core.taps;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class Tap {

    public abstract UUID uuid();

    public abstract String name();

    public abstract String description();

    @Nullable
    public abstract String version();

    @Nullable
    public abstract DateTime clock();

    @Nullable
    public abstract TotalWithAverage processedBytes();

    @Nullable
    public abstract Long memoryTotal();

    @Nullable
    public abstract Long memoryFree();

    @Nullable
    public abstract Long memoryUsed();

    @Nullable
    public abstract Double cpuLoad();

    @Nullable
    public abstract Long clockDriftMs();

    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    @Nullable
    public abstract DateTime lastReport();

    @Nullable
    public abstract UUID organizationId();

    @Nullable
    public abstract UUID tenantId();

    public static Tap create(UUID uuid, String name, String description, String version, DateTime clock, TotalWithAverage processedBytes, Long memoryTotal, Long memoryFree, Long memoryUsed, Double cpuLoad, Long clockDriftMs, DateTime createdAt, DateTime updatedAt, DateTime lastReport, UUID organizationId, UUID tenantId) {
        return builder()
                .uuid(uuid)
                .name(name)
                .description(description)
                .version(version)
                .clock(clock)
                .processedBytes(processedBytes)
                .memoryTotal(memoryTotal)
                .memoryFree(memoryFree)
                .memoryUsed(memoryUsed)
                .cpuLoad(cpuLoad)
                .clockDriftMs(clockDriftMs)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .lastReport(lastReport)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Tap.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder version(String version);

        public abstract Builder clock(DateTime clock);

        public abstract Builder processedBytes(TotalWithAverage processedBytes);

        public abstract Builder memoryTotal(Long memoryTotal);

        public abstract Builder memoryFree(Long memoryFree);

        public abstract Builder memoryUsed(Long memoryUsed);

        public abstract Builder cpuLoad(Double cpuLoad);

        public abstract Builder clockDriftMs(Long clockDriftMs);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder lastReport(DateTime lastReport);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Tap build();
    }
}
