package app.nzyme.core.rest.resources.taps.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class WiFiSupportedFrequencyReport {
    
    public abstract int frequency();
    public abstract List<String> channelWidths();

    @JsonCreator
    public static WiFiSupportedFrequencyReport create(@JsonProperty("frequency") int frequency,
                                                      @JsonProperty("channel_widths") List<String> channelWidths) {
        return builder()
                .frequency(frequency)
                .channelWidths(channelWidths)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_WiFiSupportedFrequencyReport.Builder();
    }
    
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder frequency(int frequency);

        public abstract Builder channelWidths(List<String> channelWidths);

        public abstract WiFiSupportedFrequencyReport build();
    }
}