package app.nzyme.core.ethernet.dns.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class DNSLogEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract UUID tapUUID();
    public abstract int transactionId();
    public abstract String clientAddress();
    public abstract int clientPort();
    public abstract String clientMac();
    public abstract String serverAddress();
    public abstract int serverPort();
    public abstract String serverMac();
    public abstract String dataValue();
    @Nullable
    public abstract String dataValueEtld();
    public abstract String dataType();
    public abstract String dnsType();
    public abstract DateTime timestamp();
    public abstract DateTime createdAt();

    public static DNSLogEntry create(long id, UUID uuid, UUID tapUUID, int transactionId, String clientAddress, int clientPort, String clientMac, String serverAddress, int serverPort, String serverMac, String dataValue, String dataValueEtld, String dataType, String dnsType, DateTime timestamp, DateTime createdAt) {
        return builder()
                .id(id)
                .uuid(uuid)
                .tapUUID(tapUUID)
                .transactionId(transactionId)
                .clientAddress(clientAddress)
                .clientPort(clientPort)
                .clientMac(clientMac)
                .serverAddress(serverAddress)
                .serverPort(serverPort)
                .serverMac(serverMac)
                .dataValue(dataValue)
                .dataValueEtld(dataValueEtld)
                .dataType(dataType)
                .dnsType(dnsType)
                .timestamp(timestamp)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSLogEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder tapUUID(UUID tapUUID);

        public abstract Builder transactionId(int transactionId);

        public abstract Builder clientAddress(String clientAddress);

        public abstract Builder clientPort(int clientPort);

        public abstract Builder clientMac(String clientMac);

        public abstract Builder serverAddress(String serverAddress);

        public abstract Builder serverPort(int serverPort);

        public abstract Builder serverMac(String serverMac);

        public abstract Builder dataValue(String dataValue);

        public abstract Builder dataValueEtld(String dataValueEtld);

        public abstract Builder dataType(String dataType);

        public abstract Builder dnsType(String dnsType);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract DNSLogEntry build();
    }
}
