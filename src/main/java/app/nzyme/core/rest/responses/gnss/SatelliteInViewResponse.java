package app.nzyme.core.rest.responses.gnss;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class SatelliteInViewResponse {

    @JsonProperty("constellation")
    public abstract String constellation();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("prn")
    public abstract int prn();

    @Nullable
    @JsonProperty("snr")
    public abstract Integer snr();

    @Nullable
    @JsonProperty("azimuth_degrees")
    public abstract Integer azimuthDegrees();

    @Nullable
    @JsonProperty("elevation_degrees")
    public abstract Integer elevationDegrees();

    public static SatelliteInViewResponse create(String constellation, DateTime lastSeen, int prn, Integer snr, Integer azimuthDegrees, Integer elevationDegrees) {
        return builder()
                .constellation(constellation)
                .lastSeen(lastSeen)
                .prn(prn)
                .snr(snr)
                .azimuthDegrees(azimuthDegrees)
                .elevationDegrees(elevationDegrees)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SatelliteInViewResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder constellation(String constellation);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder prn(int prn);

        public abstract Builder snr(Integer snr);

        public abstract Builder azimuthDegrees(Integer azimuthDegrees);

        public abstract Builder elevationDegrees(Integer elevationDegrees);

        public abstract SatelliteInViewResponse build();
    }
}
