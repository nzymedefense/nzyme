package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class SSIDAdvertisementHistogramValueResponse {

    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    @JsonProperty("beacon_count")
    public abstract long beaconCount();

    @JsonProperty("proberesp_count")
    public abstract long proberespCount();

    public static SSIDAdvertisementHistogramValueResponse create(DateTime timestamp, long beaconCount, long proberespCount) {
        return builder()
                .timestamp(timestamp)
                .beaconCount(beaconCount)
                .proberespCount(proberespCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDAdvertisementHistogramValueResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder beaconCount(long beaconCount);

        public abstract Builder proberespCount(long proberespCount);

        public abstract SSIDAdvertisementHistogramValueResponse build();
    }
}
