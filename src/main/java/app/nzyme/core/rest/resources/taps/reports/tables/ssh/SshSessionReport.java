package app.nzyme.core.rest.resources.taps.reports.tables.ssh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class SshSessionReport {

    public abstract SshVersionReport clientVersion();
    public abstract SshVersionReport serverVersion();
    public abstract String connectionStatus();
    public abstract int tunneledBytes();
    public abstract String sourceMac();
    public abstract String destinationMac();
    public abstract String sourceAddress();
    public abstract String destinationAddress();
    public abstract int sourcePort();
    public abstract int destinationPort();
    public abstract DateTime establishedAt();
    @Nullable
    public abstract DateTime terminatedAt();
    public abstract DateTime mostRecentSegmentTime();

    @JsonCreator
    public static SshSessionReport create(@JsonProperty("client_version") SshVersionReport clientVersion,
                                          @JsonProperty("server_version") SshVersionReport serverVersion,
                                          @JsonProperty("connection_status") String connectionStatus,
                                          @JsonProperty("tunneled_bytes")  int tunneledBytes,
                                          @JsonProperty("source_mac") String sourceMac,
                                          @JsonProperty("destination_mac")  String destinationMac,
                                          @JsonProperty("source_address") String sourceAddress,
                                          @JsonProperty("destination_address") String destinationAddress,
                                          @JsonProperty("source_port")  int sourcePort,
                                          @JsonProperty("destination_port") int destinationPort,
                                          @JsonProperty("established_at") DateTime establishedAt,
                                          @JsonProperty("terminated_at") DateTime terminatedAt,
                                          @JsonProperty("most_recent_segment_time") DateTime mostRecentSegmentTime) {
        return builder()
                .clientVersion(clientVersion)
                .serverVersion(serverVersion)
                .connectionStatus(connectionStatus)
                .tunneledBytes(tunneledBytes)
                .sourceMac(sourceMac)
                .destinationMac(destinationMac)
                .sourceAddress(sourceAddress)
                .destinationAddress(destinationAddress)
                .sourcePort(sourcePort)
                .destinationPort(destinationPort)
                .establishedAt(establishedAt)
                .terminatedAt(terminatedAt)
                .mostRecentSegmentTime(mostRecentSegmentTime)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SshSessionReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder clientVersion(SshVersionReport clientVersion);

        public abstract Builder serverVersion(SshVersionReport serverVersion);

        public abstract Builder connectionStatus(String connectionStatus);

        public abstract Builder tunneledBytes(int tunneledBytes);

        public abstract Builder sourceMac(String sourceMac);

        public abstract Builder destinationMac(String destinationMac);

        public abstract Builder sourceAddress(String sourceAddress);

        public abstract Builder destinationAddress(String destinationAddress);

        public abstract Builder sourcePort(int sourcePort);

        public abstract Builder destinationPort(int destinationPort);

        public abstract Builder establishedAt(DateTime establishedAt);

        public abstract Builder terminatedAt(DateTime terminatedAt);

        public abstract Builder mostRecentSegmentTime(DateTime mostRecentSegmentTime);

        public abstract SshSessionReport build();
    }
}


/*
#[derive(Serialize)]
pub struct SshSessionReport {
    pub client_version: SshVersionReport,
    pub server_version: SshVersionReport,
    pub connection_status: String,
    pub tunneled_bytes: u64,
    pub source_mac: String,
    pub destination_mac: String,
    pub source_address: String,
    pub source_port: u16,
    pub destination_address: String,
    pub destination_port: u16,
    pub established_at: DateTime<Utc>,
    pub terminated_at: Option<DateTime<Utc>>,
    pub most_recent_segment_time: DateTime<Utc>
}
 */