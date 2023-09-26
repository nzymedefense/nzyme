package app.nzyme.core.rest.resources.taps.reports.tables.dot11;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class Dot11DiscoTransmitterReport {

    public abstract String bssid();
    public abstract long sentFrames();
    public abstract Map<String, Long> receivers();

    @JsonCreator
    public static Dot11DiscoTransmitterReport create(@JsonProperty("bssid") String bssid,
                                                     @JsonProperty("sent_frames") long sentFrames,
                                                     @JsonProperty("receivers") Map<String, Long> receivers) {
        return builder()
                .bssid(bssid)
                .sentFrames(sentFrames)
                .receivers(receivers)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11DiscoTransmitterReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssid(String bssid);

        public abstract Builder sentFrames(long sentFrames);

        public abstract Builder receivers(Map<String, Long> receivers);

        public abstract Dot11DiscoTransmitterReport build();
    }
}
