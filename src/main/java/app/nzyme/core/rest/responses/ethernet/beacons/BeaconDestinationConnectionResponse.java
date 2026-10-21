package app.nzyme.core.rest.responses.ethernet.beacons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class BeaconDestinationConnectionResponse {

    @JsonProperty("l4_type")
    public abstract String l4Type();

    @JsonProperty("destination_port")
    public abstract int destinationPort();

    @JsonProperty("is_beacon_port")
    public abstract boolean isBeaconPort();

    @JsonProperty("session_count")
    public abstract long sessionCount();

    @JsonProperty("source_count")
    public abstract long sourceCount();

    @JsonProperty("bytes_rx")
    public abstract long bytesRx();

    @JsonProperty("bytes_tx")
    public abstract long bytesTx();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static BeaconDestinationConnectionResponse create(String l4Type, int destinationPort, boolean isBeaconPort, long sessionCount, long sourceCount, long bytesRx, long bytesTx, DateTime firstSeen, DateTime lastSeen) {
        return builder()
                .l4Type(l4Type)
                .destinationPort(destinationPort)
                .isBeaconPort(isBeaconPort)
                .sessionCount(sessionCount)
                .sourceCount(sourceCount)
                .bytesRx(bytesRx)
                .bytesTx(bytesTx)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconDestinationConnectionResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder l4Type(String l4Type);
        public abstract Builder destinationPort(int destinationPort);
        public abstract Builder isBeaconPort(boolean isBeaconPort);
        public abstract Builder sessionCount(long sessionCount);
        public abstract Builder sourceCount(long sourceCount);
        public abstract Builder bytesRx(long bytesRx);
        public abstract Builder bytesTx(long bytesTx);
        public abstract Builder firstSeen(DateTime firstSeen);
        public abstract Builder lastSeen(DateTime lastSeen);
        public abstract BeaconDestinationConnectionResponse build();
    }

}
