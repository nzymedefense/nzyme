package app.nzyme.core.rest.responses.ethernet.socks;

import app.nzyme.core.rest.responses.ethernet.L4AddressResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class SocksTunnelDetailsResponse {

    @Nullable
    @JsonProperty("client")
    public abstract L4AddressResponse client();

    @Nullable
    @JsonProperty("socks_server")
    public abstract L4AddressResponse socksServer();

    @JsonProperty("tcp_session_key")
    public abstract String tcpSessionKey();

    @JsonProperty("socks_type")
    public abstract String socksType();

    @JsonProperty("authentication_status")
    public abstract String authenticationStatus();

    @JsonProperty("handshake_status")
    public abstract String handshakeStatus();

    @JsonProperty("connection_status")
    public abstract String connectionStatus();

    @JsonProperty("username")
    @Nullable
    public abstract String username();

    @JsonProperty("tunneled_bytes")
    public abstract int tunneledBytes();

    @JsonProperty("tunneled_destination_address")
    @Nullable
    public abstract String tunneledDestinationAddress();

    @JsonProperty("tunneled_destination_host")
    @Nullable
    public abstract String tunneledDestinationHost();

    @JsonProperty("tunneled_destination_port")
    public abstract int tunneledDestinationPort();

    @JsonProperty("established_at")
    public abstract DateTime establishedAt();

    @Nullable
    @JsonProperty("terminated_at")
    public abstract DateTime terminatedAt();

    @JsonProperty("most_recent_segment_time")
    public abstract DateTime mostRecentSegmentTime();

    @JsonProperty("duration_ms")
    public abstract long durationMs();

    public static SocksTunnelDetailsResponse create(L4AddressResponse client, L4AddressResponse socksServer, String tcpSessionKey, String socksType, String authenticationStatus, String handshakeStatus, String connectionStatus, String username, int tunneledBytes, String tunneledDestinationAddress, String tunneledDestinationHost, int tunneledDestinationPort, DateTime establishedAt, DateTime terminatedAt, DateTime mostRecentSegmentTime, long durationMs) {
        return builder()
                .client(client)
                .socksServer(socksServer)
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
                .durationMs(durationMs)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SocksTunnelDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder client(L4AddressResponse client);

        public abstract Builder socksServer(L4AddressResponse socksServer);

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

        public abstract Builder durationMs(long durationMs);

        public abstract SocksTunnelDetailsResponse build();
    }
}
