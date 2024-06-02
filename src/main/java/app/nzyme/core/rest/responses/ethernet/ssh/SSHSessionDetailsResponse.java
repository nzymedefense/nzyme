package app.nzyme.core.rest.responses.ethernet.ssh;

import app.nzyme.core.rest.responses.ethernet.L4AddressResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class SSHSessionDetailsResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @Nullable
    @JsonProperty("client")
    public abstract L4AddressResponse client();

    @Nullable
    @JsonProperty("server")
    public abstract L4AddressResponse server();

    @JsonProperty("client_version")
    public abstract SSHVersionResponse clientVersion();

    @JsonProperty("server_version")
    public abstract SSHVersionResponse serverVersion();

    @JsonProperty("connection_status")
    public abstract String connectionStatus();

    @JsonProperty("tunneled_bytes")
    public abstract int tunneledBytes();

    @JsonProperty("established_at")
    public abstract DateTime establishedAt();

    @Nullable
    @JsonProperty("terminated_at")
    public abstract DateTime terminatedAt();

    @JsonProperty("most_recent_segment_time")
    public abstract DateTime mostRecentSegmentTime();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static SSHSessionDetailsResponse create(UUID uuid, L4AddressResponse client, L4AddressResponse server, SSHVersionResponse clientVersion, SSHVersionResponse serverVersion, String connectionStatus, int tunneledBytes, DateTime establishedAt, DateTime terminatedAt, DateTime mostRecentSegmentTime, DateTime updatedAt, DateTime createdAt) {
        return builder()
                .uuid(uuid)
                .client(client)
                .server(server)
                .clientVersion(clientVersion)
                .serverVersion(serverVersion)
                .connectionStatus(connectionStatus)
                .tunneledBytes(tunneledBytes)
                .establishedAt(establishedAt)
                .terminatedAt(terminatedAt)
                .mostRecentSegmentTime(mostRecentSegmentTime)
                .updatedAt(updatedAt)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSHSessionDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder client(L4AddressResponse client);

        public abstract Builder server(L4AddressResponse server);

        public abstract Builder clientVersion(SSHVersionResponse clientVersion);

        public abstract Builder serverVersion(SSHVersionResponse serverVersion);

        public abstract Builder connectionStatus(String connectionStatus);

        public abstract Builder tunneledBytes(int tunneledBytes);

        public abstract Builder establishedAt(DateTime establishedAt);

        public abstract Builder terminatedAt(DateTime terminatedAt);

        public abstract Builder mostRecentSegmentTime(DateTime mostRecentSegmentTime);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract SSHSessionDetailsResponse build();
    }
}
