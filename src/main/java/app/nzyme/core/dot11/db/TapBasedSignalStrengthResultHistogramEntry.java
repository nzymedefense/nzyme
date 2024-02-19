package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class TapBasedSignalStrengthResultHistogramEntry {

    public abstract DateTime bucket();
    public abstract UUID tapUuid();
    public abstract String tapName();
    public abstract float signalStrength();

    public static TapBasedSignalStrengthResultHistogramEntry create(DateTime bucket, UUID tapUuid, String tapName, float signalStrength) {
        return builder()
                .bucket(bucket)
                .tapUuid(tapUuid)
                .tapName(tapName)
                .signalStrength(signalStrength)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapBasedSignalStrengthResultHistogramEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder tapUuid(UUID tapUuid);

        public abstract Builder tapName(String tapName);

        public abstract Builder signalStrength(float signalStrength);

        public abstract TapBasedSignalStrengthResultHistogramEntry build();
    }
}
