package app.nzyme.core.rest.responses.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class TapBasedSignalStrengthResponse {

    @JsonProperty("tap_uuid")
    public abstract UUID tapUuid();

    @JsonProperty("tap_name")
    public abstract String tapName();

    @JsonProperty("signal_strength")
    public abstract float signalStrength();

    public static TapBasedSignalStrengthResponse create(UUID tapUuid, String tapName, float signalStrength) {
        return builder()
                .tapUuid(tapUuid)
                .tapName(tapName)
                .signalStrength(signalStrength)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapBasedSignalStrengthResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tapUuid(UUID tapUuid);

        public abstract Builder tapName(String tapName);

        public abstract Builder signalStrength(float signalStrength);

        public abstract TapBasedSignalStrengthResponse build();
    }
}
