package app.nzyme.core.rest.responses.gnss;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class SatelliteInViewResponse {

    @JsonProperty("constellation")
    public abstract String constellation();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("prn")
    public abstract int prn();

    @Nullable
    @JsonProperty("average_sno")
    public abstract Integer averageSno();

    @Nullable
    @JsonProperty("azimuth_degrees")
    public abstract Integer azimuthDegrees();

    @Nullable
    @JsonProperty("elevation_degrees")
    public abstract Integer elevationDegrees();

    @JsonProperty("used_for_fix")
    public abstract boolean usedForFix();

    @JsonProperty("average_doppler_hz")
    public abstract int averageDopplerHz();

    @JsonProperty("maximum_multipath_indicator")
    public abstract int maximumMultipathIndicator();

    @JsonProperty("average_pseudorange_rms_error")
    public abstract int averagePseudorangeRmsError();

    @JsonProperty("track_points")
    public abstract List<GNSSPRNTrackPointResponse> trackPoints();

    public static SatelliteInViewResponse create(String constellation, DateTime lastSeen, int prn, Integer averageSno, Integer azimuthDegrees, Integer elevationDegrees, boolean usedForFix, int averageDopplerHz, int maximumMultipathIndicator, int averagePseudorangeRmsError, List<GNSSPRNTrackPointResponse> trackPoints) {
        return builder()
                .constellation(constellation)
                .lastSeen(lastSeen)
                .prn(prn)
                .averageSno(averageSno)
                .azimuthDegrees(azimuthDegrees)
                .elevationDegrees(elevationDegrees)
                .usedForFix(usedForFix)
                .averageDopplerHz(averageDopplerHz)
                .maximumMultipathIndicator(maximumMultipathIndicator)
                .averagePseudorangeRmsError(averagePseudorangeRmsError)
                .trackPoints(trackPoints)
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

        public abstract Builder averageSno(Integer averageSno);

        public abstract Builder azimuthDegrees(Integer azimuthDegrees);

        public abstract Builder elevationDegrees(Integer elevationDegrees);

        public abstract Builder usedForFix(boolean usedForFix);

        public abstract Builder averageDopplerHz(int averageDopplerHz);

        public abstract Builder maximumMultipathIndicator(int maximumMultipathIndicator);

        public abstract Builder averagePseudorangeRmsError(int averagePseudorangeRmsError);

        public abstract Builder trackPoints(List<GNSSPRNTrackPointResponse> trackPoints);

        public abstract SatelliteInViewResponse build();
    }
}
