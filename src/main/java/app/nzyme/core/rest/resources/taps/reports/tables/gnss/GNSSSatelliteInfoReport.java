package app.nzyme.core.rest.resources.taps.reports.tables.gnss;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class GNSSSatelliteInfoReport {

    @JsonProperty("prn")
    public abstract int prn();

    @Nullable
    @JsonProperty("elevation_degrees")
    public abstract Integer elevationDegrees();

    @Nullable
    @JsonProperty("azimuth_degrees")
    public abstract Integer azimuthDegrees();

    @Nullable
    @JsonProperty("snr")
    public abstract Integer snr();

    @JsonCreator
    public static GNSSSatelliteInfoReport create(@JsonProperty("prn") int prn,
                                                 @JsonProperty("elevation_degrees") Integer elevationDegrees,
                                                 @JsonProperty("azimuth_degrees") Integer azimuthDegrees,
                                                 @JsonProperty("snr") Integer snr) {
        return builder()
                .prn(prn)
                .elevationDegrees(elevationDegrees)
                .azimuthDegrees(azimuthDegrees)
                .snr(snr)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSSatelliteInfoReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder prn(int prn);

        public abstract Builder elevationDegrees(Integer elevationDegrees);

        public abstract Builder azimuthDegrees(Integer azimuthDegrees);

        public abstract Builder snr(Integer snr);

        public abstract GNSSSatelliteInfoReport build();
    }
}
