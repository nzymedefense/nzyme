package app.nzyme.core.gnss.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class GNSSSatelliteInView {

    public abstract String constellation();
    public abstract DateTime lastSeen();
    public abstract int prn();
    @Nullable
    public abstract Integer averageSno();
    @Nullable
    public abstract Integer azimuthDegrees();
    @Nullable
    public abstract Integer elevationDegrees();
    public abstract boolean usedForFix();
    public abstract int averageDopplerHz();
    public abstract int maximumMultipathIndicator();
    public abstract int averagePseudorangeRmsError();

    public static GNSSSatelliteInView create(String constellation, DateTime lastSeen, int prn, Integer averageSno, Integer azimuthDegrees, Integer elevationDegrees, boolean usedForFix, int averageDopplerHz, int maximumMultipathIndicator, int averagePseudorangeRmsError) {
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
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSSatelliteInView.Builder();
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

        public abstract GNSSSatelliteInView build();
    }
}
