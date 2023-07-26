package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.Min;

@AutoValue
public abstract class CreateDot11MonitoredChannelRequest {

    @Min(0)
    public abstract long frequency();

    @JsonCreator
    public static CreateDot11MonitoredChannelRequest create(@JsonProperty("frequency") long frequency) {
        return builder()
                .frequency(frequency)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateDot11MonitoredChannelRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder frequency(long frequency);

        public abstract CreateDot11MonitoredChannelRequest build();
    }
}
