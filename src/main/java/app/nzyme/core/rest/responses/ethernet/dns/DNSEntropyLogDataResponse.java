package app.nzyme.core.rest.responses.ethernet.dns;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class DNSEntropyLogDataResponse {

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("tap_uuid")
    public abstract UUID tapUUID();

    @JsonProperty("transaction_id")
    public abstract int transactionId();

    @JsonProperty("client_address")
    public abstract String clientAddress();

    @JsonProperty("client_port")
    public abstract int clientPort();

    @JsonProperty("client_mac")
    public abstract String clientMac();

    @JsonProperty("server_address")
    public abstract String serverAddress();

    @JsonProperty("server_port")
    public abstract int serverPort();

    @JsonProperty("server_mac")
    public abstract String serverMac();

    @JsonProperty("data_value")
    public abstract String dataValue();

    @Nullable
    @JsonProperty("data_value_etld")
    public abstract String dataValueEtld();

    @JsonProperty("data_type")
    public abstract String dataType();

    @JsonProperty("dns_type")
    public abstract String dnsType();

    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static DNSEntropyLogDataResponse create(UUID uuid, UUID tapUUID, int transactionId, String clientAddress, int clientPort, String clientMac, String serverAddress, int serverPort, String serverMac, String dataValue, String dataValueEtld, String dataType, String dnsType, DateTime timestamp, DateTime createdAt) {
        return builder()
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
        return new AutoValue_DNSEntropyLogDataResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
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

        public abstract DNSEntropyLogDataResponse build();
    }
}
