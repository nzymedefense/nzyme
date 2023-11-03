package app.nzyme.core.rest.responses.dot11.disco;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class Dot11DiscoMonitorAnomalyDetailsResponse {

    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    @JsonProperty("frame_count")
    public abstract long frameCount();

    public static Dot11DiscoMonitorAnomalyDetailsResponse create(DateTime timestamp, long frameCount) {
        return builder()
                .timestamp(timestamp)
                .frameCount(frameCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11DiscoMonitorAnomalyDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder frameCount(long frameCount);

        public abstract Dot11DiscoMonitorAnomalyDetailsResponse build();
    }
}
