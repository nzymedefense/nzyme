package app.nzyme.core.rest.resources.taps.reports.tables.tls;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class TlsCertificateReport {

    public abstract String fingerprintSha256();
    public abstract long derLength();
    public abstract boolean parsed();
    public abstract String subject();
    public abstract String issuer();
    public abstract String serial();
    public abstract List<String> subjectAlternativeNames();
    @Nullable
    public abstract DateTime notBefore();
    @Nullable
    public abstract DateTime notAfter();
    public abstract boolean selfSigned();
    public abstract boolean isCa();
    public abstract String publicKeyAlgorithm();
    public abstract long publicKeyBits();
    public abstract String signatureAlgorithm();

    @JsonCreator
    public static TlsCertificateReport create(@JsonProperty("fingerprint_sha256") String fingerprintSha256,
                                              @JsonProperty("der_length") long derLength,
                                              @JsonProperty("parsed") boolean parsed,
                                              @JsonProperty("subject") String subject,
                                              @JsonProperty("issuer") String issuer,
                                              @JsonProperty("serial") String serial,
                                              @JsonProperty("subject_alternative_names") List<String> subjectAlternativeNames,
                                              @JsonProperty("not_before") DateTime notBefore,
                                              @JsonProperty("not_after") DateTime notAfter,
                                              @JsonProperty("self_signed") boolean selfSigned,
                                              @JsonProperty("is_ca") boolean isCa,
                                              @JsonProperty("public_key_algorithm") String publicKeyAlgorithm,
                                              @JsonProperty("public_key_bits") long publicKeyBits,
                                              @JsonProperty("signature_algorithm") String signatureAlgorithm) {
        return builder()
                .fingerprintSha256(fingerprintSha256)
                .derLength(derLength)
                .parsed(parsed)
                .subject(subject)
                .issuer(issuer)
                .serial(serial)
                .subjectAlternativeNames(subjectAlternativeNames)
                .notBefore(notBefore)
                .notAfter(notAfter)
                .selfSigned(selfSigned)
                .isCa(isCa)
                .publicKeyAlgorithm(publicKeyAlgorithm)
                .publicKeyBits(publicKeyBits)
                .signatureAlgorithm(signatureAlgorithm)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TlsCertificateReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder fingerprintSha256(String fingerprintSha256);

        public abstract Builder derLength(long derLength);

        public abstract Builder parsed(boolean parsed);

        public abstract Builder subject(String subject);

        public abstract Builder issuer(String issuer);

        public abstract Builder serial(String serial);

        public abstract Builder subjectAlternativeNames(List<String> subjectAlternativeNames);

        public abstract Builder notBefore(DateTime notBefore);

        public abstract Builder notAfter(DateTime notAfter);

        public abstract Builder selfSigned(boolean selfSigned);

        public abstract Builder isCa(boolean isCa);

        public abstract Builder publicKeyAlgorithm(String publicKeyAlgorithm);

        public abstract Builder publicKeyBits(long publicKeyBits);

        public abstract Builder signatureAlgorithm(String signatureAlgorithm);

        public abstract TlsCertificateReport build();
    }
}
