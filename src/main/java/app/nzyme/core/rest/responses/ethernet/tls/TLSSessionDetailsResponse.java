package app.nzyme.core.rest.responses.ethernet.tls;

import app.nzyme.core.rest.responses.ethernet.L4AddressResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class TLSSessionDetailsResponse {

    @JsonProperty("tcp_session_key")
    public abstract String tcpSessionKey();

    @Nullable
    @JsonProperty("client")
    public abstract L4AddressResponse client();

    @Nullable
    @JsonProperty("server")
    public abstract L4AddressResponse server();

    @JsonProperty("connection_status")
    public abstract String connectionStatus();

    @JsonProperty("bytes_count")
    public abstract long bytesCount();

    @Nullable
    @JsonProperty("server_name")
    public abstract String serverName();

    @Nullable
    @JsonProperty("version")
    public abstract String version();

    @Nullable
    @JsonProperty("client_max_version")
    public abstract String clientMaxVersion();

    @Nullable
    @JsonProperty("cipher_suite")
    public abstract Integer cipherSuite();

    @Nullable
    @JsonProperty("alpn_selected")
    public abstract String alpnSelected();

    @JsonProperty("alpn_offered")
    public abstract List<String> alpnOffered();

    @JsonProperty("session_resumed")
    public abstract boolean sessionResumed();

    @JsonProperty("hello_retry_request")
    public abstract boolean helloRetryRequest();

    @JsonProperty("encrypted_client_hello")
    public abstract boolean encryptedClientHello();

    @JsonProperty("client_certificate_requested")
    public abstract boolean clientCertificateRequested();

    @Nullable
    @JsonProperty("server_leaf_sha256")
    public abstract String serverLeafSha256();

    @Nullable
    @JsonProperty("server_leaf_common_name")
    public abstract String serverLeafCommonName();

    @Nullable
    @JsonProperty("server_leaf_issuer")
    public abstract String serverLeafIssuer();

    @Nullable
    @JsonProperty("server_leaf_not_after")
    public abstract DateTime serverLeafNotAfter();

    @Nullable
    @JsonProperty("server_chain_length")
    public abstract Integer serverChainLength();

    @Nullable
    @JsonProperty("client_leaf_sha256")
    public abstract String clientLeafSha256();

    @Nullable
    @JsonProperty("server_leaf_self_signed")
    public abstract Boolean serverLeafSelfSigned();

    @Nullable
    @JsonProperty("sni_matches_certificate")
    public abstract Boolean sniMatchesCertificate();

    @Nullable
    @JsonProperty("leaf_valid_at_handshake")
    public abstract Boolean leafValidAtHandshake();

    @Nullable
    @JsonProperty("chain_trusted")
    public abstract Boolean chainTrusted();

    @Nullable
    @JsonProperty("chain_trust_error")
    public abstract String chainTrustError();

    @JsonProperty("established_at")
    public abstract DateTime establishedAt();

    @Nullable
    @JsonProperty("terminated_at")
    public abstract DateTime terminatedAt();

    @JsonProperty("most_recent_segment_time")
    public abstract DateTime mostRecentSegmentTime();

    @JsonProperty("duration_ms")
    public abstract long durationMs();

    public static Builder builder() {
        return new AutoValue_TLSSessionDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tcpSessionKey(String tcpSessionKey);
        public abstract Builder client(L4AddressResponse client);
        public abstract Builder server(L4AddressResponse server);
        public abstract Builder connectionStatus(String connectionStatus);
        public abstract Builder bytesCount(long bytesCount);
        public abstract Builder serverName(String serverName);
        public abstract Builder version(String version);
        public abstract Builder clientMaxVersion(String clientMaxVersion);
        public abstract Builder cipherSuite(Integer cipherSuite);
        public abstract Builder alpnSelected(String alpnSelected);
        public abstract Builder alpnOffered(List<String> alpnOffered);
        public abstract Builder sessionResumed(boolean sessionResumed);
        public abstract Builder helloRetryRequest(boolean helloRetryRequest);
        public abstract Builder encryptedClientHello(boolean encryptedClientHello);
        public abstract Builder clientCertificateRequested(boolean clientCertificateRequested);
        public abstract Builder serverLeafSha256(String serverLeafSha256);
        public abstract Builder serverLeafCommonName(String serverLeafCommonName);
        public abstract Builder serverLeafIssuer(String serverLeafIssuer);
        public abstract Builder serverLeafNotAfter(DateTime serverLeafNotAfter);
        public abstract Builder serverChainLength(Integer serverChainLength);
        public abstract Builder clientLeafSha256(String clientLeafSha256);
        public abstract Builder serverLeafSelfSigned(Boolean serverLeafSelfSigned);
        public abstract Builder sniMatchesCertificate(Boolean sniMatchesCertificate);
        public abstract Builder leafValidAtHandshake(Boolean leafValidAtHandshake);
        public abstract Builder chainTrusted(Boolean chainTrusted);
        public abstract Builder chainTrustError(String chainTrustError);
        public abstract Builder establishedAt(DateTime establishedAt);
        public abstract Builder terminatedAt(DateTime terminatedAt);
        public abstract Builder mostRecentSegmentTime(DateTime mostRecentSegmentTime);
        public abstract Builder durationMs(long durationMs);
        public abstract TLSSessionDetailsResponse build();
    }
}
