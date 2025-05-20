package app.nzyme.core.ethernet.ssh.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class SSHSessionEntry {

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

    public static SSHSessionEntry create(String tcpSessionKey, String clientVersionVersion, String clientVersionSoftware, String clientVersionComments, String serverVersionVersion, String serverVersionSoftware, String serverVersionComments, String connectionStatus, int tunneledBytes, DateTime establishedAt, DateTime terminatedAt, DateTime mostRecentSegmentTime) {
        return builder()
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
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSHSessionEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
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

        public abstract SSHSessionEntry build();
    }
}
