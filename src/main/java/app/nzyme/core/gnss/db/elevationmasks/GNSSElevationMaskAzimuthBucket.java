package app.nzyme.core.gnss.db.elevationmasks;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

// NOTE: We are using this type in both the read of the raw satellite data as well as the aggregated data in the table.

@AutoValue
public abstract class GNSSElevationMaskAzimuthBucket {

    public abstract UUID tapUuid();
    public abstract int azimuthBucket();
    @Nullable
    public abstract Double skylineElevation();
    public abstract double skylineElevationBestEffort();
    public abstract int lowSubsetCount();
    public abstract double minElevationObserved();
    public abstract boolean usedFallback();
    public abstract double snoMedian();
    public abstract double snoP10();
    public abstract int sampleCount();
    public abstract DateTime windowStart();
    public abstract DateTime windowEnd();

    public static GNSSElevationMaskAzimuthBucket create(UUID tapUuid, int azimuthBucket, Double skylineElevation, double skylineElevationBestEffort, int lowSubsetCount, double minElevationObserved, boolean usedFallback, double snoMedian, double snoP10, int sampleCount, DateTime windowStart, DateTime windowEnd) {
        return builder()
                .tapUuid(tapUuid)
                .azimuthBucket(azimuthBucket)
                .skylineElevation(skylineElevation)
                .skylineElevationBestEffort(skylineElevationBestEffort)
                .lowSubsetCount(lowSubsetCount)
                .minElevationObserved(minElevationObserved)
                .usedFallback(usedFallback)
                .snoMedian(snoMedian)
                .snoP10(snoP10)
                .sampleCount(sampleCount)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSElevationMaskAzimuthBucket.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tapUuid(UUID tapUuid);

        public abstract Builder azimuthBucket(int azimuthBucket);

        public abstract Builder skylineElevation(Double skylineElevation);

        public abstract Builder skylineElevationBestEffort(double skylineElevationBestEffort);

        public abstract Builder lowSubsetCount(int lowSubsetCount);

        public abstract Builder minElevationObserved(double minElevationObserved);

        public abstract Builder usedFallback(boolean usedFallback);

        public abstract Builder snoMedian(double snoMedian);

        public abstract Builder snoP10(double snoP10);

        public abstract Builder sampleCount(int sampleCount);

        public abstract Builder windowStart(DateTime windowStart);

        public abstract Builder windowEnd(DateTime windowEnd);

        public abstract GNSSElevationMaskAzimuthBucket build();
    }
}
