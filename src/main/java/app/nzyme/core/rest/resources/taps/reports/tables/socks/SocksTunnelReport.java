package app.nzyme.core.rest.resources.taps.reports.tables.socks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class SocksTunnelReport {

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
    public abstract String sourceMac();
    public abstract String destinationMac();
    public abstract String sourceAddress();
    public abstract int sourcePort();
    public abstract String destinationAddress();
    public abstract int destinationPort();
    public abstract DateTime establishedAt();
    @Nullable
    public abstract DateTime terminatedAt();
    public abstract DateTime mostRecentSegmentTime();

    @JsonCreator
    public static SocksTunnelReport create(@JsonProperty("socks_type") String socksType,
                                           @JsonProperty("authentication_status") String authenticationStatus,
                                           @JsonProperty("handshake_status") String handshakeStatus,
                                           @JsonProperty("connection_status") String connectionStatus,
                                           @JsonProperty("username") String username,
                                           @JsonProperty("tunneled_bytes") int tunneledBytes,
                                           @JsonProperty("tunneled_destination_address") String tunneledDestinationAddress,
                                           @JsonProperty("tunneled_destination_host") String tunneledDestinationHost,
                                           @JsonProperty("tunneled_destination_port") int tunneledDestinationPort,
                                           @JsonProperty("source_mac") String sourceMac,
                                           @JsonProperty("destination_mac") String destinationMac,
                                           @JsonProperty("source_address") String sourceAddress,
                                           @JsonProperty("source_port") int sourcePort,
                                           @JsonProperty("destination_address") String destinationAddress,
                                           @JsonProperty("destination_port") int destinationPort,
                                           @JsonProperty("established_at") DateTime establishedAt,
                                           @JsonProperty("terminated_at") DateTime terminatedAt,
                                           @JsonProperty("most_recent_segment_time") DateTime mostRecentSegmentTime) {
        return builder()
                .socksType(socksType)
                .authenticationStatus(authenticationStatus)
                .handshakeStatus(handshakeStatus)
                .connectionStatus(connectionStatus)
                .username(username)
                .tunneledBytes(tunneledBytes)
                .tunneledDestinationAddress(tunneledDestinationAddress)
                .tunneledDestinationHost(tunneledDestinationHost)
                .tunneledDestinationPort(tunneledDestinationPort)
                .sourceMac(sourceMac)
                .destinationMac(destinationMac)
                .sourceAddress(sourceAddress)
                .sourcePort(sourcePort)
                .destinationAddress(destinationAddress)
                .destinationPort(destinationPort)
                .establishedAt(establishedAt)
                .terminatedAt(terminatedAt)
                .mostRecentSegmentTime(mostRecentSegmentTime)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SocksTunnelReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder socksType(String socksType);

        public abstract Builder authenticationStatus(String authenticationStatus);

        public abstract Builder handshakeStatus(String handshakeStatus);

        public abstract Builder connectionStatus(String connectionStatus);

        public abstract Builder username(String username);

        public abstract Builder tunneledBytes(int tunneledBytes);

        public abstract Builder tunneledDestinationAddress(String tunneledDestinationAddress);

        public abstract Builder tunneledDestinationHost(String tunneledDestinationHost);

        public abstract Builder tunneledDestinationPort(int tunneledDestinationPort);

        public abstract Builder sourceMac(String sourceMac);

        public abstract Builder destinationMac(String destinationMac);

        public abstract Builder sourceAddress(String sourceAddress);

        public abstract Builder sourcePort(int sourcePort);

        public abstract Builder destinationAddress(String destinationAddress);

        public abstract Builder destinationPort(int destinationPort);

        public abstract Builder establishedAt(DateTime establishedAt);

        public abstract Builder terminatedAt(DateTime terminatedAt);

        public abstract Builder mostRecentSegmentTime(DateTime mostRecentSegmentTime);

        public abstract SocksTunnelReport build();
    }
}
