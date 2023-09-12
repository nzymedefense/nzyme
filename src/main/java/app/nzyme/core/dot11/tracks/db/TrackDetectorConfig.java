package app.nzyme.core.dot11.tracks.db;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TrackDetectorConfig {

    public abstract int frameThreshold();
    public abstract int gapThreshold();
    public abstract int signalCenterlineJitter();

    public static TrackDetectorConfig create(int frameThreshold, int gapThreshold, int signalCenterlineJitter) {
        return builder()
                .frameThreshold(frameThreshold)
                .gapThreshold(gapThreshold)
                .signalCenterlineJitter(signalCenterlineJitter)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TrackDetectorConfig.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder frameThreshold(int frameThreshold);

        public abstract Builder gapThreshold(int gapThreshold);

        public abstract Builder signalCenterlineJitter(int signalCenterlineJitter);

        public abstract TrackDetectorConfig build();
    }
}
