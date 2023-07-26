package app.nzyme.core.rest.responses.dot11.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class MonitoredChannelResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("frequency")
    public abstract long frequency();

    @JsonProperty("channel")
    public abstract long channel();

    public static MonitoredChannelResponse create(UUID uuid, long frequency, long channel) {
        return builder()
                .uuid(uuid)
                .frequency(frequency)
                .channel(channel)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MonitoredChannelResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder frequency(long frequency);

        public abstract Builder channel(long channel);

        public abstract MonitoredChannelResponse build();
    }
}
