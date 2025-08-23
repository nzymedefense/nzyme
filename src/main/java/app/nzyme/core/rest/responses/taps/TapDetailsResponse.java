package app.nzyme.core.rest.responses.taps;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class TapDetailsResponse {

    @JsonProperty
    public abstract UUID uuid();

    @JsonProperty("name")
    public abstract String name();

    @Nullable
    @JsonProperty("version")
    public abstract String version();

    @Nullable
    @JsonProperty("clock")
    public abstract DateTime clock();

    @Nullable
    @JsonProperty("processed_bytes")
    public abstract TotalWithAverageResponse processedBytes();

    @Nullable
    @JsonProperty("memory_total")
    public abstract Long memoryTotal();

    @Nullable
    @JsonProperty("memory_free")
    public abstract Long memoryFree();

    @Nullable
    @JsonProperty("memory_used")
    public abstract Long memoryUsed();

    @Nullable
    @JsonProperty("cpu_load")
    public abstract Double cpuLoad();

    @JsonProperty("active")
    public abstract boolean active();

    @Nullable
    @JsonProperty("clock_drift_ms")
    public abstract Long clockDriftMs();

    @Nullable
    @JsonProperty("rpi")
    public abstract String rpi();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @Nullable
    @JsonProperty("last_report")
    public abstract DateTime lastReport();

    @JsonProperty("description")
    public abstract String description();

    @JsonProperty("buses")
    public abstract List<BusDetailsResponse> buses();

    @JsonProperty("captures")
    public abstract List<CaptureDetailsResponse> captures();

    @JsonProperty("remote_address")
    public abstract String remoteAddress();

    @JsonProperty("dot11_frequencies")
    public abstract List<TapFrequencyAndChannelWidthsResponse> dot11Frequencies();

    @JsonProperty("organization_id")
    public abstract UUID organizationId();

    @JsonProperty("tenant_id")
    public abstract UUID tenantId();

    @Nullable
    @JsonProperty("location_id")
    public abstract UUID locationId();

    @Nullable
    @JsonProperty("location_name")
    public abstract String locationName();

    @Nullable
    @JsonProperty("floor_id")
    public abstract UUID floorId();

    @Nullable
    @JsonProperty("floor_name")
    public abstract String floorName();

    @JsonProperty("latitude")
    @Nullable
    public abstract Double latitude();

    @JsonProperty("longitude")
    @Nullable
    public abstract Double longitude();

    public static TapDetailsResponse create(UUID uuid, String name, String version, DateTime clock, TotalWithAverageResponse processedBytes, Long memoryTotal, Long memoryFree, Long memoryUsed, Double cpuLoad, boolean active, Long clockDriftMs, String rpi, DateTime createdAt, DateTime updatedAt, DateTime lastReport, String description, List<BusDetailsResponse> buses, List<CaptureDetailsResponse> captures, String remoteAddress, List<TapFrequencyAndChannelWidthsResponse> dot11Frequencies, UUID organizationId, UUID tenantId, UUID locationId, String locationName, UUID floorId, String floorName, Double latitude, Double longitude) {
        return builder()
                .uuid(uuid)
                .name(name)
                .version(version)
                .clock(clock)
                .processedBytes(processedBytes)
                .memoryTotal(memoryTotal)
                .memoryFree(memoryFree)
                .memoryUsed(memoryUsed)
                .cpuLoad(cpuLoad)
                .active(active)
                .clockDriftMs(clockDriftMs)
                .rpi(rpi)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .lastReport(lastReport)
                .description(description)
                .buses(buses)
                .captures(captures)
                .remoteAddress(remoteAddress)
                .dot11Frequencies(dot11Frequencies)
                .organizationId(organizationId)
                .tenantId(tenantId)
                .locationId(locationId)
                .locationName(locationName)
                .floorId(floorId)
                .floorName(floorName)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder name(String name);

        public abstract Builder version(String version);

        public abstract Builder clock(DateTime clock);

        public abstract Builder processedBytes(TotalWithAverageResponse processedBytes);

        public abstract Builder memoryTotal(Long memoryTotal);

        public abstract Builder memoryFree(Long memoryFree);

        public abstract Builder memoryUsed(Long memoryUsed);

        public abstract Builder cpuLoad(Double cpuLoad);

        public abstract Builder active(boolean active);

        public abstract Builder clockDriftMs(Long clockDriftMs);

        public abstract Builder rpi(String rpi);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder lastReport(DateTime lastReport);

        public abstract Builder description(String description);

        public abstract Builder buses(List<BusDetailsResponse> buses);

        public abstract Builder captures(List<CaptureDetailsResponse> captures);

        public abstract Builder remoteAddress(String remoteAddress);

        public abstract Builder dot11Frequencies(List<TapFrequencyAndChannelWidthsResponse> dot11Frequencies);

        public abstract Builder organizationId(UUID organizationId);

        public abstract Builder tenantId(UUID tenantId);

        public abstract Builder locationId(UUID locationId);

        public abstract Builder locationName(String locationName);

        public abstract Builder floorId(UUID floorId);

        public abstract Builder floorName(String floorName);

        public abstract Builder latitude(Double latitude);

        public abstract Builder longitude(Double longitude);

        public abstract TapDetailsResponse build();
    }
}
