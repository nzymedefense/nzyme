package app.nzyme.core.ethernet.l4.tcp.db;

import app.nzyme.core.ethernet.L4AddressData;
import app.nzyme.core.ethernet.L4Type;
import app.nzyme.core.ethernet.l4.tcp.TcpSessionState;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class TcpSessionEntry {

    public abstract long id();
    public abstract String sessionKey();
    public abstract UUID tapUuid();
    public abstract L4Type l4Type();
    public abstract L4AddressData source();
    public abstract L4AddressData destination();
    public abstract long bytesRxCount();
    public abstract long bytesTxCount();
    public abstract long segmentsCount();
    public abstract DateTime startTime();
    @Nullable
    public abstract DateTime endTime();
    public abstract DateTime mostRecentSegmentTime();
    public abstract TcpSessionState state();
    public abstract DateTime createdAt();

    public static TcpSessionEntry create(long id, String sessionKey, UUID tapUuid, L4Type l4Type, L4AddressData source, L4AddressData destination, long bytesRxCount, long bytesTxCount, long segmentsCount, DateTime startTime, DateTime endTime, DateTime mostRecentSegmentTime, TcpSessionState state, DateTime createdAt) {
        return builder()
                .id(id)
                .sessionKey(sessionKey)
                .tapUuid(tapUuid)
                .l4Type(l4Type)
                .source(source)
                .destination(destination)
                .bytesRxCount(bytesRxCount)
                .bytesTxCount(bytesTxCount)
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

        public abstract Builder sessionKey(String sessionKey);

        public abstract Builder tapUuid(UUID tapUuid);

        public abstract Builder l4Type(L4Type l4Type);

        public abstract Builder source(L4AddressData source);

        public abstract Builder destination(L4AddressData destination);

        public abstract Builder bytesRxCount(long bytesRxCount);

        public abstract Builder bytesTxCount(long bytesTxCount);

        public abstract Builder segmentsCount(long segmentsCount);

        public abstract Builder startTime(DateTime startTime);

        public abstract Builder endTime(DateTime endTime);

        public abstract Builder mostRecentSegmentTime(DateTime mostRecentSegmentTime);

        public abstract Builder state(TcpSessionState state);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract TcpSessionEntry build();
    }
}
