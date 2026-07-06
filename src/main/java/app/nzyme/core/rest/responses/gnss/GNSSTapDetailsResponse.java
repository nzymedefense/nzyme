package app.nzyme.core.rest.responses.gnss;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class GNSSTapDetailsResponse {

    @JsonProperty("id")
    public abstract UUID uuid();

    @JsonProperty("name")
    public abstract String name();

    @Nullable
    @JsonProperty("location_uuid")
    public abstract UUID locationUuid();

    @Nullable
    @JsonProperty("location_name")
    public abstract String locationName();

    @Nullable
    @JsonProperty("fix_quality_histogram")
    public abstract Map<DateTime, GNSSStringBucketResponse> fixQualityHistogram();

    @Nullable
    @JsonProperty("beidou_distance")
    public abstract Double beidouDistance();

    @Nullable
    @JsonProperty("galileo_distance")
    public abstract Double galileoDistance();

    @Nullable
    @JsonProperty("glonass_distance")
    public abstract Double glonassDistance();

    @Nullable
    @JsonProperty("gps_distance")
    public abstract Double gpsDistance();

    public static GNSSTapDetailsResponse create(UUID uuid, String name, UUID locationUuid, String locationName, Map<DateTime, GNSSStringBucketResponse> fixQualityHistogram, Double beidouDistance, Double galileoDistance, Double glonassDistance, Double gpsDistance) {
        return builder()
                .uuid(uuid)
                .name(name)
                .locationUuid(locationUuid)
                .locationName(locationName)
                .fixQualityHistogram(fixQualityHistogram)
                .beidouDistance(beidouDistance)
                .galileoDistance(galileoDistance)
                .glonassDistance(glonassDistance)
                .gpsDistance(gpsDistance)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSTapDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder name(String name);

        public abstract Builder locationUuid(UUID locationUuid);

        public abstract Builder locationName(String locationName);

        public abstract Builder fixQualityHistogram(Map<DateTime, GNSSStringBucketResponse> fixQualityHistogram);

        public abstract Builder beidouDistance(Double beidouDistance);

        public abstract Builder galileoDistance(Double galileoDistance);

        public abstract Builder glonassDistance(Double glonassDistance);

        public abstract Builder gpsDistance(Double gpsDistance);

        public abstract GNSSTapDetailsResponse build();
    }
}
