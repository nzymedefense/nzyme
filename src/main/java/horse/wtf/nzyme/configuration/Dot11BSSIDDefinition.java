package horse.wtf.nzyme.configuration;

import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.dot11.networks.signalstrength.tracks.TrackDetector;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
public abstract class Dot11BSSIDDefinition {

    public abstract String address();
    public abstract List<String> fingerprints();

    @Nullable
    public abstract TrackDetector.TrackDetectorConfig trackDetectorConfig();

    public static Dot11BSSIDDefinition create(String address, List<String> fingerprints, TrackDetector.TrackDetectorConfig trackDetectorConfig) {
        return builder()
                .address(address)
                .fingerprints(fingerprints)
                .trackDetectorConfig(trackDetectorConfig)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11BSSIDDefinition.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder address(String address);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Builder trackDetectorConfig(TrackDetector.TrackDetectorConfig trackDetectorConfig);

        public abstract Dot11BSSIDDefinition build();
    }

}
