package app.nzyme.core.ethernet.socks.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class SocksTunnelEntry {

    public abstract String tcpSessionKey();
    public abstract String socksType();
    public abstract String authenticationStatus();
    public abstract String handshakeStatus();
    public abstract String connectionStatus();
    @Nullable
    public abstract String username();
    public abstract int tunneledBytes();
    @Nullable
    public abstract String tunneledDestinationAddress();
    @Nullable
    public abstract String tunneledDestinationHost();
    public abstract int tunneledDestinationPort();
    public abstract DateTime establishedAt();
    @Nullable
    public abstract DateTime terminatedAt();
    public abstract DateTime mostRecentSegmentTime();

    public static SocksTunnelEntry create(String tcpSessionKey, String socksType, String authenticationStatus, String handshakeStatus, String connectionStatus, String username, int tunneledBytes, String tunneledDestinationAddress, String tunneledDestinationHost, int tunneledDestinationPort, DateTime establishedAt, DateTime terminatedAt, DateTime mostRecentSegmentTime) {
        return builder()
                .tcpSessionKey(tcpSessionKey)
                .socksType(socksType)
                .authenticationStatus(authenticationStatus)
                .handshakeStatus(handshakeStatus)
                .connectionStatus(connectionStatus)
                .username(username)
                .tunneledBytes(tunneledBytes)
                .tunneledDestinationAddress(tunneledDestinationAddress)
                .tunneledDestinationHost(tunneledDestinationHost)
                .tunneledDestinationPort(tunneledDestinationPort)
                .establishedAt(establishedAt)
                .terminatedAt(terminatedAt)
                .mostRecentSegmentTime(mostRecentSegmentTime)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SocksTunnelEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder tcpSessionKey(String tcpSessionKey);

        public abstract Builder socksType(String socksType);

        public abstract Builder authenticationStatus(String authenticationStatus);

        public abstract Builder handshakeStatus(String handshakeStatus);

        public abstract Builder connectionStatus(String connectionStatus);

        public abstract Builder username(String username);

        public abstract Builder tunneledBytes(int tunneledBytes);

        public abstract Builder tunneledDestinationAddress(String tunneledDestinationAddress);

        public abstract Builder tunneledDestinationHost(String tunneledDestinationHost);

        public abstract Builder tunneledDestinationPort(int tunneledDestinationPort);

        public abstract Builder establishedAt(DateTime establishedAt);

        public abstract Builder terminatedAt(DateTime terminatedAt);

        public abstract Builder mostRecentSegmentTime(DateTime mostRecentSegmentTime);

        public abstract SocksTunnelEntry build();
    }
}
