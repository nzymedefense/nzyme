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
    @JsonProperty("average_sno")
    public abstract Integer averageSno();

    @Nullable
    @JsonProperty("average_doppler_hz")
    public abstract Integer averageDopplerHz();

    @Nullable
    @JsonProperty("maximum_multipath_indicator")
    public abstract Integer maximumMultipathIndicator();

    @Nullable
    @JsonProperty("average_pseurange_rms_err")
    public abstract Integer averagePseudorangeRmsError();

    @JsonCreator
    public static GNSSSatelliteInfoReport create(@JsonProperty("prn") int prn,
                                                 @JsonProperty("elevation_degrees") Integer elevationDegrees,
                                                 @JsonProperty("azimuth_degrees") Integer azimuthDegrees,
                                                 @JsonProperty("average_sno") Integer averageSno,
                                                 @JsonProperty("average_doppler_hz") Integer averageDopplerHz,
                                                 @JsonProperty("maximum_multipath_indicator") Integer maximumMultipathIndicator,
                                                 @JsonProperty("average_pseurange_rms_err") Integer averagePseudorangeRmsError) {
        return builder()
                .prn(prn)
                .elevationDegrees(elevationDegrees)
                .azimuthDegrees(azimuthDegrees)
                .averageSno(averageSno)
                .averageDopplerHz(averageDopplerHz)
                .maximumMultipathIndicator(maximumMultipathIndicator)
                .averagePseudorangeRmsError(averagePseudorangeRmsError)
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

        public abstract Builder averageSno(Integer averageSno);

        public abstract Builder averageDopplerHz(Integer averageDopplerHz);

        public abstract Builder maximumMultipathIndicator(Integer maximumMultipathIndicator);

        public abstract Builder averagePseudorangeRmsError(Integer averagePseudorangeRmsError);

        public abstract GNSSSatelliteInfoReport build();
    }
}