package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class BSSIDAndSSIDHistogramValueResponse {

    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    @JsonProperty("bssid_count")
    public abstract long bssidCount();

    @JsonProperty("ssid_count")
    public abstract long ssidCount();

    public static BSSIDAndSSIDHistogramValueResponse create(DateTime timestamp, long bssidCount, long ssidCount) {
        return builder()
                .timestamp(timestamp)
                .bssidCount(bssidCount)
                .ssidCount(ssidCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDAndSSIDHistogramValueResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder bssidCount(long bssidCount);

        public abstract Builder ssidCount(long ssidCount);

        public abstract BSSIDAndSSIDHistogramValueResponse build();
    }
}
