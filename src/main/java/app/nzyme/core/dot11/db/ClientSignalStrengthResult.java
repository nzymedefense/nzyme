package app.nzyme.core.dot11.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ClientSignalStrengthResult {

    public abstract DateTime bucket();
    public abstract Double signalStrength();

    public static ClientSignalStrengthResult create(DateTime bucket, Double signalStrength) {
        return builder()
                .bucket(bucket)
                .signalStrength(signalStrength)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClientSignalStrengthResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder signalStrength(Double signalStrength);

        public abstract ClientSignalStrengthResult build();
    }
}
