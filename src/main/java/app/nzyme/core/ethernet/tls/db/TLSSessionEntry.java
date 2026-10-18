package app.nzyme.core.ethernet.tls.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class TLSSessionEntry {

    public abstract String tcpSessionKey();
    @Nullable
    public abstract String sourceMac();
    @Nullable
    public abstract String destinationMac();
    public abstract String sourceAddress();
    public abstract int sourcePort();
    public abstract String destinationAddress();
    public abstract int destinationPort();
    public abstract String connectionStatus();
    public abstract long bytesCount();
    @Nullable
    public abstract String serverName();
    @Nullable
    public abstract String version();
    @Nullable
    public abstract String clientMaxVersion();
    @Nullable
    public abstract Integer cipherSuite();
    @Nullable
    public abstract String alpnSelected();
    public abstract List<String> alpnOffered();
    @Nullable
    public abstract String ja4();
    public abstract boolean sessionResumed();
    public abstract boolean helloRetryRequest();
    public abstract boolean encryptedClientHello();
    public abstract boolean clientCertificateRequested();
    @Nullable
    public abstract String serverLeafSha256();
    @Nullable
    public abstract String serverLeafCommonName();
    @Nullable
    public abstract String serverLeafIssuer();
    @Nullable
    public abstract DateTime serverLeafNotAfter();
    @Nullable
    public abstract Integer serverChainLength();
    @Nullable
    public abstract String clientLeafSha256();
    @Nullable
    public abstract Boolean serverLeafSelfSigned();
    @Nullable
    public abstract Boolean sniMatchesCertificate();
    @Nullable
    public abstract Boolean leafValidAtHandshake();
    @Nullable
    public abstract Boolean chainTrusted();
    @Nullable
    public abstract String chainTrustError();
    public abstract DateTime establishedAt();
    @Nullable
    public abstract DateTime terminatedAt();
    public abstract DateTime mostRecentSegmentTime();
    public abstract long durationMs();

    public static Builder builder() {
        return new AutoValue_TLSSessionEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tcpSessionKey(String tcpSessionKey);
        public abstract Builder sourceMac(String sourceMac);
        public abstract Builder destinationMac(String destinationMac);
        public abstract Builder sourceAddress(String sourceAddress);
        public abstract Builder sourcePort(int sourcePort);
        public abstract Builder destinationAddress(String destinationAddress);
        public abstract Builder destinationPort(int destinationPort);
        public abstract Builder connectionStatus(String connectionStatus);
        public abstract Builder bytesCount(long bytesCount);
        public abstract Builder serverName(String serverName);
        public abstract Builder version(String version);
        public abstract Builder clientMaxVersion(String clientMaxVersion);
        public abstract Builder cipherSuite(Integer cipherSuite);
        public abstract Builder alpnSelected(String alpnSelected);
        public abstract Builder alpnOffered(List<String> alpnOffered);
        public abstract Builder ja4(String ja4);
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
        public abstract TLSSessionEntry build();
    }
}
