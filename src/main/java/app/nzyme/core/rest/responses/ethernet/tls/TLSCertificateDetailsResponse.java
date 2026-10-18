package app.nzyme.core.rest.responses.ethernet.tls;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class TLSCertificateDetailsResponse {

    @JsonProperty("sha256")
    public abstract String sha256();

    @Nullable
    @JsonProperty("sha1")
    public abstract String sha1();

    @JsonProperty("parsed")
    public abstract boolean parsed();

    @Nullable
    @JsonProperty("subject")
    public abstract String subject();

    @Nullable
    @JsonProperty("issuer")
    public abstract String issuer();

    @Nullable
    @JsonProperty("common_name")
    public abstract String commonName();

    @JsonProperty("subject_alternative_names")
    public abstract List<String> subjectAlternativeNames();

    @Nullable
    @JsonProperty("not_before")
    public abstract DateTime notBefore();

    @Nullable
    @JsonProperty("not_after")
    public abstract DateTime notAfter();

    @Nullable
    @JsonProperty("is_ca")
    public abstract Boolean isCa();

    @Nullable
    @JsonProperty("self_signed")
    public abstract Boolean selfSigned();

    @Nullable
    @JsonProperty("public_key_algorithm")
    public abstract String publicKeyAlgorithm();

    @Nullable
    @JsonProperty("public_key_bits")
    public abstract Integer publicKeyBits();

    @Nullable
    @JsonProperty("signature_algorithm")
    public abstract String signatureAlgorithm();

    @JsonProperty("extended_key_usage")
    public abstract List<String> extendedKeyUsage();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("session_count")
    public abstract long sessionCount();

    @JsonProperty("sources")
    public abstract List<String> sources();

    public static Builder builder() {
        return new AutoValue_TLSCertificateDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sha256(String sha256);
        public abstract Builder sha1(String sha1);
        public abstract Builder parsed(boolean parsed);
        public abstract Builder subject(String subject);
        public abstract Builder issuer(String issuer);
        public abstract Builder commonName(String commonName);
        public abstract Builder subjectAlternativeNames(List<String> subjectAlternativeNames);
        public abstract Builder notBefore(DateTime notBefore);
        public abstract Builder notAfter(DateTime notAfter);
        public abstract Builder isCa(Boolean isCa);
        public abstract Builder selfSigned(Boolean selfSigned);
        public abstract Builder publicKeyAlgorithm(String publicKeyAlgorithm);
        public abstract Builder publicKeyBits(Integer publicKeyBits);
        public abstract Builder signatureAlgorithm(String signatureAlgorithm);
        public abstract Builder extendedKeyUsage(List<String> extendedKeyUsage);
        public abstract Builder firstSeen(DateTime firstSeen);
        public abstract Builder lastSeen(DateTime lastSeen);
        public abstract Builder sessionCount(long sessionCount);
        public abstract Builder sources(List<String> sources);
        public abstract TLSCertificateDetailsResponse build();
    }
}
