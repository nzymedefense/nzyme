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
    public abstract Integer snr();
    @Nullable
    public abstract Integer azimuthDegrees();
    @Nullable
    public abstract Integer elevationDegrees();

    public static GNSSSatelliteInView create(String constellation, DateTime lastSeen, int prn, Integer snr, Integer azimuthDegrees, Integer elevationDegrees) {
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
        return new AutoValue_GNSSSatelliteInView.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder constellation(String constellation);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder prn(int prn);

        public abstract Builder snr(Integer snr);

        public abstract Builder azimuthDegrees(Integer azimuthDegrees);

        public abstract Builder elevationDegrees(Integer elevationDegrees);

        public abstract GNSSSatelliteInView build();
    }
}
