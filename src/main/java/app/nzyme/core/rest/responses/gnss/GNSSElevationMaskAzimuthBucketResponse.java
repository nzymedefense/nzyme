package app.nzyme.core.rest.responses.gnss;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class GNSSElevationMaskAzimuthBucketResponse {

    @JsonProperty("azimuth_bucket")
    public abstract int azimuthBucket();
    @Nullable
    @JsonProperty("skyline_elevation")
    public abstract Double skylineElevation();
    @JsonProperty("skyline_elevation_best_effort")
    public abstract double skylineElevationBestEffort();
    @JsonProperty("low_subset_count")
    public abstract int lowSubsetCount();
    @JsonProperty("min_elevation_observed")
    public abstract double minElevationObserved();
    @JsonProperty("used_fallback")
    public abstract boolean usedFallback();
    @JsonProperty("sno_median")
    public abstract double cn0Median();
    @JsonProperty("sno_p10")
    public abstract double cn0P10();
    @JsonProperty("sample_count")
    public abstract int sampleCount();
    @JsonProperty("window_start")
    public abstract DateTime windowStart();
    @JsonProperty("window_end")
    public abstract DateTime windowEnd();

    public static GNSSElevationMaskAzimuthBucketResponse create(int azimuthBucket, Double skylineElevation, double skylineElevationBestEffort, int lowSubsetCount, double minElevationObserved, boolean usedFallback, double cn0Median, double cn0P10, int sampleCount, DateTime windowStart, DateTime windowEnd) {
        return builder()
                .azimuthBucket(azimuthBucket)
                .skylineElevation(skylineElevation)
                .skylineElevationBestEffort(skylineElevationBestEffort)
                .lowSubsetCount(lowSubsetCount)
                .minElevationObserved(minElevationObserved)
                .usedFallback(usedFallback)
                .cn0Median(cn0Median)
                .cn0P10(cn0P10)
                .sampleCount(sampleCount)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSElevationMaskAzimuthBucketResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder azimuthBucket(int azimuthBucket);

        public abstract Builder skylineElevation(Double skylineElevation);

        public abstract Builder skylineElevationBestEffort(double skylineElevationBestEffort);

        public abstract Builder lowSubsetCount(int lowSubsetCount);

        public abstract Builder minElevationObserved(double minElevationObserved);

        public abstract Builder usedFallback(boolean usedFallback);

        public abstract Builder cn0Median(double cn0Median);

        public abstract Builder cn0P10(double cn0P10);

        public abstract Builder sampleCount(int sampleCount);

        public abstract Builder windowStart(DateTime windowStart);

        public abstract Builder windowEnd(DateTime windowEnd);

        public abstract GNSSElevationMaskAzimuthBucketResponse build();
    }
}
