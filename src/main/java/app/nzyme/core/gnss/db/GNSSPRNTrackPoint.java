package app.nzyme.core.gnss.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class GNSSPRNTrackPoint {

    @Nullable
    public abstract Integer averageSno();
    @Nullable
    public abstract Integer azimuthDegrees();
    @Nullable
    public abstract Integer elevationDegrees();
    @Nullable
    public abstract DateTime timestamp();

    public static GNSSPRNTrackPoint create(Integer averageSno, Integer azimuthDegrees, Integer elevationDegrees, DateTime timestamp) {
        return builder()
                .averageSno(averageSno)
                .azimuthDegrees(azimuthDegrees)
                .elevationDegrees(elevationDegrees)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSPRNTrackPoint.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder averageSno(Integer averageSno);

        public abstract Builder azimuthDegrees(Integer azimuthDegrees);

        public abstract Builder elevationDegrees(Integer elevationDegrees);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract GNSSPRNTrackPoint build();
    }
}
