package app.nzyme.core.taps;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class Tap {

    public abstract long id();

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

    @Nullable
    public abstract String rpi();

    @Nullable
    public abstract Double rpiTemperature();

    public abstract DateTime createdAt();
    public abstract DateTime updatedAt();

    @Nullable
    public abstract DateTime lastReport();

    @Nullable
    public abstract UUID organizationId();

    @Nullable
    public abstract UUID tenantId();

    @Nullable
    public abstract UUID locationId();

    @Nullable
    public abstract UUID floorId();

    @Nullable
    public abstract Integer x();

    @Nullable
    public abstract Integer y();

    public abstract String remoteAddress();

    @Nullable
    public abstract Double latitude();

    @Nullable
    public abstract Double longitude();

    public static Tap create(long id, UUID uuid, String name, String description, String version, DateTime clock, TotalWithAverage processedBytes, Long memoryTotal, Long memoryFree, Long memoryUsed, Double cpuLoad, Long clockDriftMs, String rpi, Double rpiTemperature, DateTime createdAt, DateTime updatedAt, DateTime lastReport, UUID organizationId, UUID tenantId, UUID locationId, UUID floorId, Integer x, Integer y, String remoteAddress, Double latitude, Double longitude) {
        return builder()
                .id(id)
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
                .rpi(rpi)
                .rpiTemperature(rpiTemperature)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .lastReport(lastReport)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .locationId(locationId)
                .floorId(floorId)
                .x(x)
                .y(y)
                .remoteAddress(remoteAddress)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Tap.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

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

        public abstract Builder rpi(String rpi);

        public abstract Builder rpiTemperature(Double rpiTemperature);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder lastReport(DateTime lastReport);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder locationId(UUID locationId);

        public abstract Builder floorId(UUID floorId);

        public abstract Builder x(Integer x);

        public abstract Builder y(Integer y);

        public abstract Builder remoteAddress(String remoteAddress);

        public abstract Builder latitude(Double latitude);

        public abstract Builder longitude(Double longitude);

        public abstract Tap build();
    }
}
