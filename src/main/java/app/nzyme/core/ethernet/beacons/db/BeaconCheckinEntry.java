package app.nzyme.core.ethernet.beacons.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import jakarta.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class BeaconCheckinEntry {

    public abstract long id();
    public abstract long beaconId();
    public abstract long beaconSourceId();
    public abstract DateTime timestamp();
    @Nullable
    public abstract String destinationAddress();
    @Nullable
    public abstract String destinationMac();
    @Nullable
    public abstract UUID tapUuid();
    @Nullable
    public abstract String sessionKey();
    @Nullable
    public abstract String l4Type();
    public abstract int sessionCount();
    @Nullable
    public abstract Long bytesRx();
    @Nullable
    public abstract Long bytesTx();
    @Nullable
    public abstract Long bytesTotal();

    public static BeaconCheckinEntry create(long id, long beaconId, long beaconSourceId, DateTime timestamp, String destinationAddress, String destinationMac, UUID tapUuid, String sessionKey, String l4Type, int sessionCount, Long bytesRx, Long bytesTx, Long bytesTotal) {
        return builder()
                .id(id)
                .beaconId(beaconId)
                .beaconSourceId(beaconSourceId)
                .timestamp(timestamp)
                .destinationAddress(destinationAddress)
                .destinationMac(destinationMac)
                .tapUuid(tapUuid)
                .sessionKey(sessionKey)
                .l4Type(l4Type)
                .sessionCount(sessionCount)
                .bytesRx(bytesRx)
                .bytesTx(bytesTx)
                .bytesTotal(bytesTotal)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BeaconCheckinEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);
        public abstract Builder beaconId(long beaconId);
        public abstract Builder beaconSourceId(long beaconSourceId);
        public abstract Builder timestamp(DateTime timestamp);
        public abstract Builder destinationAddress(String destinationAddress);
        public abstract Builder destinationMac(String destinationMac);
        public abstract Builder tapUuid(UUID tapUuid);
        public abstract Builder sessionKey(String sessionKey);
        public abstract Builder l4Type(String l4Type);
        public abstract Builder sessionCount(int sessionCount);
        public abstract Builder bytesRx(Long bytesRx);
        public abstract Builder bytesTx(Long bytesTx);
        public abstract Builder bytesTotal(Long bytesTotal);
        public abstract BeaconCheckinEntry build();
    }

}
