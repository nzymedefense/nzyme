package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class BeaconCheckinPointResponse {

    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    @JsonProperty("source_uuid")
    public abstract UUID sourceUuid();

    @JsonProperty("source_address")
    public abstract String sourceAddress();

    @Nullable
    @JsonProperty("bytes_total")
    public abstract Long bytesTotal();

    @Nullable
    @JsonProperty("interval_seconds")
    public abstract Double intervalSeconds();

    public static BeaconCheckinPointResponse create(DateTime timestamp, UUID sourceUuid, String sourceAddress, Long bytesTotal, Double intervalSeconds) {
        return builder()
                .timestamp(timestamp)
                .sourceUuid(sourceUuid)
                .sourceAddress(sourceAddress)
                .bytesTotal(bytesTotal)
                .intervalSeconds(intervalSeconds)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconCheckinPointResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder timestamp(DateTime timestamp);
        public abstract Builder sourceUuid(UUID sourceUuid);
        public abstract Builder sourceAddress(String sourceAddress);
        public abstract Builder bytesTotal(Long bytesTotal);
        public abstract Builder intervalSeconds(Double intervalSeconds);
        public abstract BeaconCheckinPointResponse build();
    }

}
