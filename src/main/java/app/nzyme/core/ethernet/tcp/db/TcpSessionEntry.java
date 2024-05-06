package app.nzyme.core.ethernet.tcp.db;

import app.nzyme.core.ethernet.L4Type;
import app.nzyme.core.ethernet.tcp.TcpSessionState;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class TcpSessionEntry {

    public abstract long id();
    public abstract UUID tapUuid();
    public abstract L4Type l4Type();
    public abstract String sourceMac();
    public abstract String sourceAddress();
    public abstract int sourcePort();
    public abstract String destinationMac();
    public abstract String destinationAddress();
    public abstract int destinationPort();
    public abstract long bytesCount();
    public abstract long segmentsCount();
    public abstract DateTime startTime();
    @Nullable
    public abstract DateTime endTime();
    public abstract DateTime mostRecentSegmentTime();
    public abstract TcpSessionState state();
    public abstract DateTime createdAt();

    public static TcpSessionEntry create(long id, UUID tapUuid, L4Type l4Type, String sourceMac, String sourceAddress, int sourcePort, String destinationMac, String destinationAddress, int destinationPort, long bytesCount, long segmentsCount, DateTime startTime, DateTime endTime, DateTime mostRecentSegmentTime, TcpSessionState state, DateTime createdAt) {
        return builder()
                .id(id)
                .tapUuid(tapUuid)
                .l4Type(l4Type)
                .sourceMac(sourceMac)
                .sourceAddress(sourceAddress)
                .sourcePort(sourcePort)
                .destinationMac(destinationMac)
                .destinationAddress(destinationAddress)
                .destinationPort(destinationPort)
                .bytesCount(bytesCount)
                .segmentsCount(segmentsCount)
                .startTime(startTime)
                .endTime(endTime)
                .mostRecentSegmentTime(mostRecentSegmentTime)
                .state(state)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TcpSessionEntry.Builder();
    }
    
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder tapUuid(UUID tapUuid);

        public abstract Builder l4Type(L4Type l4Type);

        public abstract Builder sourceMac(String sourceMac);

        public abstract Builder sourceAddress(String sourceAddress);

        public abstract Builder sourcePort(int sourcePort);

        public abstract Builder destinationMac(String destinationMac);

        public abstract Builder destinationAddress(String destinationAddress);

        public abstract Builder destinationPort(int destinationPort);

        public abstract Builder bytesCount(long bytesCount);

        public abstract Builder segmentsCount(long segmentsCount);

        public abstract Builder startTime(DateTime startTime);

        public abstract Builder endTime(DateTime endTime);

        public abstract Builder mostRecentSegmentTime(DateTime mostRecentSegmentTime);

        public abstract Builder state(TcpSessionState state);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract TcpSessionEntry build();
    }
}
