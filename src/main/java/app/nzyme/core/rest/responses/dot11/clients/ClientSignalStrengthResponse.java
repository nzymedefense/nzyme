package app.nzyme.core.rest.responses.dot11.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ClientSignalStrengthResponse {

    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    @JsonProperty("signal_strength")
    public abstract long signalStrength();

    public static ClientSignalStrengthResponse create(DateTime timestamp, long signalStrength) {
        return builder()
                .timestamp(timestamp)
                .signalStrength(signalStrength)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ClientSignalStrengthResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder signalStrength(long signalStrength);

        public abstract ClientSignalStrengthResponse build();
    }
}
