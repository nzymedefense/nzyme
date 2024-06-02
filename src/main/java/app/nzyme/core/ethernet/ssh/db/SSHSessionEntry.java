package app.nzyme.core.ethernet.ssh.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class SSHSessionEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract UUID tapUUID();
    public abstract String tcpSessionKey();
    public abstract String clientVersionVersion();
    public abstract String clientVersionSoftware();
    @Nullable
    public abstract String clientVersionComments();
    public abstract String serverVersionVersion();
    public abstract String serverVersionSoftware();
    @Nullable
    public abstract String serverVersionComments();
    public abstract String connectionStatus();
    public abstract int tunneledBytes();
    public abstract DateTime establishedAt();
    @Nullable
    public abstract DateTime terminatedAt();
    public abstract DateTime mostRecentSegmentTime();
    public abstract DateTime updatedAt();
    public abstract DateTime createdAt();

    public static SSHSessionEntry create(long id, UUID uuid, UUID tapUUID, String tcpSessionKey, String clientVersionVersion, String clientVersionSoftware, String clientVersionComments, String serverVersionVersion, String serverVersionSoftware, String serverVersionComments, String connectionStatus, int tunneledBytes, DateTime establishedAt, DateTime terminatedAt, DateTime mostRecentSegmentTime, DateTime updatedAt, DateTime createdAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .tapUUID(tapUUID)
                .tcpSessionKey(tcpSessionKey)
                .clientVersionVersion(clientVersionVersion)
                .clientVersionSoftware(clientVersionSoftware)
                .clientVersionComments(clientVersionComments)
                .serverVersionVersion(serverVersionVersion)
                .serverVersionSoftware(serverVersionSoftware)
                .serverVersionComments(serverVersionComments)
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
        return new AutoValue_SSHSessionEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder tapUUID(UUID tapUUID);

        public abstract Builder tcpSessionKey(String tcpSessionKey);

        public abstract Builder clientVersionVersion(String clientVersionVersion);

        public abstract Builder clientVersionSoftware(String clientVersionSoftware);

        public abstract Builder clientVersionComments(String clientVersionComments);

        public abstract Builder serverVersionVersion(String serverVersionVersion);

        public abstract Builder serverVersionSoftware(String serverVersionSoftware);

        public abstract Builder serverVersionComments(String serverVersionComments);

        public abstract Builder connectionStatus(String connectionStatus);

        public abstract Builder tunneledBytes(int tunneledBytes);

        public abstract Builder establishedAt(DateTime establishedAt);

        public abstract Builder terminatedAt(DateTime terminatedAt);

        public abstract Builder mostRecentSegmentTime(DateTime mostRecentSegmentTime);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract SSHSessionEntry build();
    }
}
