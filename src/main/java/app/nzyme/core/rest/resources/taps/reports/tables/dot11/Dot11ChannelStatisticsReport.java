package app.nzyme.core.rest.resources.taps.reports.tables.dot11;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Dot11ChannelStatisticsReport {

    public abstract long bytes();
    public abstract long frames();

    @JsonCreator
    public static Dot11ChannelStatisticsReport create(@JsonProperty("bytes") long bytes,
                                                      @JsonProperty("frames") long frames) {
        return builder()
                .bytes(bytes)
                .frames(frames)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11ChannelStatisticsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bytes(long bytes);

        public abstract Builder frames(long frames);

        public abstract Dot11ChannelStatisticsReport build();
    }
}
