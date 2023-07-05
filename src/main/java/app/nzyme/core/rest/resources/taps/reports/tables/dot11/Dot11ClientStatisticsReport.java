package app.nzyme.core.rest.resources.taps.reports.tables.dot11;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Dot11ClientStatisticsReport {

    public abstract long txFrames();
    public abstract long txBytes();
    public abstract long rxFrames();
    public abstract long rxBytes();

    @JsonCreator
    public static Dot11ClientStatisticsReport create(@JsonProperty("tx_frames") long txFrames,
                                                     @JsonProperty("tx_bytes") long txBytes,
                                                     @JsonProperty("rx_frames") long rxFrames,
                                                     @JsonProperty("rx_bytes") long rxBytes) {
        return builder()
                .txFrames(txFrames)
                .txBytes(txBytes)
                .rxFrames(rxFrames)
                .rxBytes(rxBytes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11ClientStatisticsReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder txFrames(long txFrames);

        public abstract Builder txBytes(long txBytes);

        public abstract Builder rxFrames(long rxFrames);

        public abstract Builder rxBytes(long rxBytes);

        public abstract Dot11ClientStatisticsReport build();
    }

}
