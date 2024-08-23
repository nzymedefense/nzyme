package app.nzyme.core.shared.db;

import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class TapBasedSignalStrengthResult {

    public abstract UUID tapUuid();
    public abstract String tapName();
    public abstract float signalStrength();

    public static TapBasedSignalStrengthResult create(UUID tapUuid, String tapName, float signalStrength) {
        return builder()
                .tapUuid(tapUuid)
                .tapName(tapName)
                .signalStrength(signalStrength)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapBasedSignalStrengthResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tapUuid(UUID tapUuid);

        public abstract Builder tapName(String tapName);

        public abstract Builder signalStrength(float signalStrength);

        public abstract TapBasedSignalStrengthResult build();
    }
}
