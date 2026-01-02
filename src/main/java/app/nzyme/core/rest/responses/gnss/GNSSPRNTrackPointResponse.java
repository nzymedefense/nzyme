package app.nzyme.core.rest.responses.gnss;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class GNSSPRNTrackPointResponse {

    @Nullable
    @JsonProperty("average_sno")
    public abstract Integer averageSno();

    @Nullable
    @JsonProperty("azimuth_degrees")
    public abstract Integer azimuthDegrees();

    @Nullable
    @JsonProperty("elevation_degrees")
    public abstract Integer elevationDegrees();

    @Nullable
    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    public static GNSSPRNTrackPointResponse create(Integer averageSno, Integer azimuthDegrees, Integer elevationDegrees, DateTime timestamp) {
        return builder()
                .averageSno(averageSno)
                .azimuthDegrees(azimuthDegrees)
                .elevationDegrees(elevationDegrees)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSPRNTrackPointResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder averageSno(Integer averageSno);

        public abstract Builder azimuthDegrees(Integer azimuthDegrees);

        public abstract Builder elevationDegrees(Integer elevationDegrees);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract GNSSPRNTrackPointResponse build();
    }
}
