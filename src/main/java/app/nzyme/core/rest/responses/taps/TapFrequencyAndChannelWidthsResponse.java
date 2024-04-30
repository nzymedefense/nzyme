package app.nzyme.core.rest.responses.taps;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TapFrequencyAndChannelWidthsResponse {

    @JsonProperty("frequency")
    public abstract int frequency();

    @JsonProperty("channel_widths")
    public abstract List<String> channelWidths();

    public static TapFrequencyAndChannelWidthsResponse create(int frequency, List<String> channelWidths) {
        return builder()
                .frequency(frequency)
                .channelWidths(channelWidths)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapFrequencyAndChannelWidthsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder frequency(int frequency);

        public abstract Builder channelWidths(List<String> channelWidths);

        public abstract TapFrequencyAndChannelWidthsResponse build();
    }

}
