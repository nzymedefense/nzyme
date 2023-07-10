package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ActiveChannelDetailsResponse {

    @JsonProperty("channel")
    public abstract int channel();

    @JsonProperty("frequency")
    public abstract int frequency();

    @JsonProperty("frames")
    public abstract long frames();

    @JsonProperty("bytes")
    public abstract long bytes();

    public static ActiveChannelDetailsResponse create(int channel, int frequency, long frames, long bytes) {
        return builder()
                .channel(channel)
                .frequency(frequency)
                .frames(frames)
                .bytes(bytes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ActiveChannelDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder channel(int channel);

        public abstract Builder frequency(int frequency);

        public abstract Builder frames(long frames);

        public abstract Builder bytes(long bytes);

        public abstract ActiveChannelDetailsResponse build();
    }
}
