package app.nzyme.core.ethernet.beacons;

import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

public class BeaconCheckinRecord {

    private final DateTime timestamp;
    @Nullable private final String l4Type;

    @Nullable private String destinationAddress;
    @Nullable private String destinationMac;
    @Nullable private String tapUuid;
    @Nullable private String sessionKey;
    private int sessionCount = 0;
    @Nullable private Long bytesRx;
    @Nullable private Long bytesTx;
    @Nullable private Long bytesTotal;

    public BeaconCheckinRecord(DateTime timestamp, @Nullable String l4Type) {
        this.timestamp = timestamp;
        this.l4Type = l4Type;
    }

    public void addConnection(@Nullable String destinationAddress,
                              @Nullable String destinationMac,
                              @Nullable String tapUuid,
                              @Nullable String sessionKey,
                              @Nullable Long bytesRx,
                              @Nullable Long bytesTx,
                              @Nullable Long bytesTotal) {
        sessionCount++;

        if (this.destinationAddress == null) {
            this.destinationAddress = destinationAddress;
        }
        if (this.destinationMac == null) {
            this.destinationMac = destinationMac;
        }
        if (this.tapUuid == null) {
            this.tapUuid = tapUuid;
        }
        if (this.sessionKey == null) {
            this.sessionKey = sessionKey;
        }

        this.bytesRx = add(this.bytesRx, bytesRx);
        this.bytesTx = add(this.bytesTx, bytesTx);
        this.bytesTotal = add(this.bytesTotal, bytesTotal);
    }

    @Nullable
    private static Long add(@Nullable Long current, @Nullable Long value) {
        if (value == null) {
            return current;
        }

        return current == null ? value : current + value;
    }

    public DateTime timestamp() {
        return timestamp;
    }

    @Nullable
    public String l4Type() {
        return l4Type;
    }

    @Nullable
    public String destinationAddress() {
        return destinationAddress;
    }

    @Nullable
    public String destinationMac() {
        return destinationMac;
    }

    @Nullable
    public String tapUuid() {
        return tapUuid;
    }

    @Nullable
    public String sessionKey() {
        return sessionKey;
    }

    public int sessionCount() {
        return Math.max(1, sessionCount);
    }

    @Nullable
    public Long bytesRx() {
        return bytesRx;
    }

    @Nullable
    public Long bytesTx() {
        return bytesTx;
    }

    /**
     * Total bytes. Uses the explicit total if the data source provided one, otherwise the sum of RX and TX.
     */
    @Nullable
    public Long bytesTotal() {
        if (bytesTotal != null) {
            return bytesTotal;
        }

        if (bytesRx == null && bytesTx == null) {
            return null;
        }

        return (bytesRx == null ? 0 : bytesRx) + (bytesTx == null ? 0 : bytesTx);
    }

}
