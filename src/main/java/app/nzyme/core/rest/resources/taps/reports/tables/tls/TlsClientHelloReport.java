package app.nzyme.core.rest.resources.taps.reports.tables.tls;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.List;

@AutoValue
public abstract class TlsClientHelloReport {

    public abstract String recordVersion();
    public abstract String legacyVersion();
    public abstract List<String> supportedVersions();
    @Nullable
    public abstract String serverName();
    public abstract List<Integer> cipherSuites();
    public abstract List<Integer> extensions();
    public abstract List<Integer> supportedGroups();
    public abstract List<Integer> ecPointFormats();
    public abstract List<Integer> signatureAlgorithms();
    public abstract List<String> alpn();
    public abstract boolean sessionIdPresent();
    public abstract boolean offersSessionTicket();
    public abstract boolean offersPreSharedKey();
    public abstract boolean encryptedClientHello();

    @JsonCreator
    public static TlsClientHelloReport create(@JsonProperty("record_version") String recordVersion,
                                              @JsonProperty("legacy_version") String legacyVersion,
                                              @JsonProperty("supported_versions") List<String> supportedVersions,
                                              @JsonProperty("server_name") String serverName,
                                              @JsonProperty("cipher_suites") List<Integer> cipherSuites,
                                              @JsonProperty("extensions") List<Integer> extensions,
                                              @JsonProperty("supported_groups") List<Integer> supportedGroups,
                                              @JsonProperty("ec_point_formats") List<Integer> ecPointFormats,
                                              @JsonProperty("signature_algorithms") List<Integer> signatureAlgorithms,
                                              @JsonProperty("alpn") List<String> alpn,
                                              @JsonProperty("session_id_present") boolean sessionIdPresent,
                                              @JsonProperty("offers_session_ticket") boolean offersSessionTicket,
                                              @JsonProperty("offers_pre_shared_key") boolean offersPreSharedKey,
                                              @JsonProperty("encrypted_client_hello") boolean encryptedClientHello) {
        return builder()
                .recordVersion(recordVersion)
                .legacyVersion(legacyVersion)
                .supportedVersions(supportedVersions)
                .serverName(serverName)
                .cipherSuites(cipherSuites)
                .extensions(extensions)
                .supportedGroups(supportedGroups)
                .ecPointFormats(ecPointFormats)
                .signatureAlgorithms(signatureAlgorithms)
                .alpn(alpn)
                .sessionIdPresent(sessionIdPresent)
                .offersSessionTicket(offersSessionTicket)
                .offersPreSharedKey(offersPreSharedKey)
                .encryptedClientHello(encryptedClientHello)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TlsClientHelloReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder recordVersion(String recordVersion);

        public abstract Builder legacyVersion(String legacyVersion);

        public abstract Builder supportedVersions(List<String> supportedVersions);

        public abstract Builder serverName(String serverName);

        public abstract Builder cipherSuites(List<Integer> cipherSuites);

        public abstract Builder extensions(List<Integer> extensions);

        public abstract Builder supportedGroups(List<Integer> supportedGroups);

        public abstract Builder ecPointFormats(List<Integer> ecPointFormats);

        public abstract Builder signatureAlgorithms(List<Integer> signatureAlgorithms);

        public abstract Builder alpn(List<String> alpn);

        public abstract Builder sessionIdPresent(boolean sessionIdPresent);

        public abstract Builder offersSessionTicket(boolean offersSessionTicket);

        public abstract Builder offersPreSharedKey(boolean offersPreSharedKey);

        public abstract Builder encryptedClientHello(boolean encryptedClientHello);

        public abstract TlsClientHelloReport build();
    }
}
