package app.nzyme.core.rest.resources.taps.reports.tables.gnss;

/*
#[derive(Serialize)]
pub struct SatelliteInfoReport {
    pub prn: u8,
    pub elevation_degrees: u8,
    pub azimuth_degrees: u16,
    pub snr_db: Option<u8>,
}
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class GNSSSatelliteInfoReport {

    public abstract int prn();
    public abstract int elevationDegrees();
    public abstract int azimuthDegrees();
    @Nullable
    public abstract Integer snrDb();

    @JsonCreator
    public static GNSSSatelliteInfoReport create(@JsonProperty("prn") int prn,
                                                 @JsonProperty("elevation_degrees") int elevationDegrees,
                                                 @JsonProperty("azimuth_degrees") int azimuthDegrees,
                                                 @JsonProperty("snr_db") Integer snrDb) {
        return builder()
                .prn(prn)
                .elevationDegrees(elevationDegrees)
                .azimuthDegrees(azimuthDegrees)
                .snrDb(snrDb)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSSatelliteInfoReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder prn(int prn);

        public abstract Builder elevationDegrees(int elevationDegrees);

        public abstract Builder azimuthDegrees(int azimuthDegrees);

        public abstract Builder snrDb(Integer snrDb);

        public abstract GNSSSatelliteInfoReport build();
    }
}
