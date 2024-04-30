package app.nzyme.core.taps.db.metrics;

import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class Dot11FrequencyAndChannelWidthEntry {

    public abstract long id();
    public abstract UUID interfaceUuid();
    public abstract int frequency();
    public abstract List<String> channelWidths();

    public static Dot11FrequencyAndChannelWidthEntry create(long id, UUID interfaceUuid, int frequency, List<String> channelWidths) {
        return builder()
                .id(id)
                .interfaceUuid(interfaceUuid)
                .frequency(frequency)
                .channelWidths(channelWidths)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11FrequencyAndChannelWidthEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder interfaceUuid(UUID interfaceUuid);

        public abstract Builder frequency(int frequency);

        public abstract Builder channelWidths(List<String> channelWidths);

        public abstract Dot11FrequencyAndChannelWidthEntry build();
    }

}
