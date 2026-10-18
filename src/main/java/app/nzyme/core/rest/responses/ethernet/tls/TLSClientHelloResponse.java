package app.nzyme.core.rest.responses.ethernet.tls;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

import java.util.List;

@AutoValue
public abstract class TLSClientHelloResponse {

    @Nullable
    @JsonProperty("record_version")
    public abstract String recordVersion();

    @Nullable
    @JsonProperty("legacy_version")
    public abstract String legacyVersion();

    @JsonProperty("supported_versions")
    public abstract List<String> supportedVersions();

    @JsonProperty("cipher_suites")
    public abstract List<Integer> cipherSuites();

    @JsonProperty("extensions")
    public abstract List<Integer> extensions();

    @JsonProperty("supported_groups")
    public abstract List<Integer> supportedGroups();

    @JsonProperty("ec_point_formats")
    public abstract List<Integer> ecPointFormats();

    @JsonProperty("signature_algorithms")
    public abstract List<Integer> signatureAlgorithms();

    @JsonProperty("alpn")
    public abstract List<String> alpn();

    @JsonProperty("session_id_present")
    public abstract boolean sessionIdPresent();

    @JsonProperty("offers_session_ticket")
    public abstract boolean offersSessionTicket();

    @JsonProperty("offers_pre_shared_key")
    public abstract boolean offersPreSharedKey();

    public static Builder builder() {
        return new AutoValue_TLSClientHelloResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder recordVersion(String recordVersion);
        public abstract Builder legacyVersion(String legacyVersion);
        public abstract Builder supportedVersions(List<String> supportedVersions);
        public abstract Builder cipherSuites(List<Integer> cipherSuites);
        public abstract Builder extensions(List<Integer> extensions);
        public abstract Builder supportedGroups(List<Integer> supportedGroups);
        public abstract Builder ecPointFormats(List<Integer> ecPointFormats);
        public abstract Builder signatureAlgorithms(List<Integer> signatureAlgorithms);
        public abstract Builder alpn(List<String> alpn);
        public abstract Builder sessionIdPresent(boolean sessionIdPresent);
        public abstract Builder offersSessionTicket(boolean offersSessionTicket);
        public abstract Builder offersPreSharedKey(boolean offersPreSharedKey);
        public abstract TLSClientHelloResponse build();
    }
}
