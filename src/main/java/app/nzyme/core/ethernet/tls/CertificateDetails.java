package app.nzyme.core.ethernet.tls;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.security.cert.X509Certificate;
import java.util.List;

@AutoValue
public abstract class CertificateDetails {

    public abstract byte[] der();
    public abstract byte[] sha256();
    public abstract byte[] sha1();
    @Nullable
    public abstract X509Certificate certificate();
    @Nullable
    public abstract String parseError();
    @Nullable
    public abstract Integer version();
    @Nullable
    public abstract String serial();
    @Nullable
    public abstract String subject();
    @Nullable
    public abstract String issuer();
    @Nullable
    public abstract String commonName();
    public abstract List<String> subjectAlternativeNames();
    public abstract List<String> dnsNames();
    @Nullable
    public abstract DateTime notBefore();
    @Nullable
    public abstract DateTime notAfter();
    @Nullable
    public abstract Boolean isCa();
    @Nullable
    public abstract Boolean selfSigned();
    @Nullable
    public abstract String publicKeyAlgorithm();
    @Nullable
    public abstract Integer publicKeyBits();
    @Nullable
    public abstract String signatureAlgorithm();
    public abstract List<String> extendedKeyUsage();
    @Nullable
    public abstract byte[] subjectKeyIdentifier();
    @Nullable
    public abstract byte[] authorityKeyIdentifier();

    public boolean parsed() {
        return certificate() != null;
    }

    public static Builder builder() {
        return new AutoValue_CertificateDetails.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder der(byte[] der);

        public abstract Builder sha256(byte[] sha256);

        public abstract Builder sha1(byte[] sha1);

        public abstract Builder certificate(X509Certificate certificate);

        public abstract Builder parseError(String parseError);

        public abstract Builder version(Integer version);

        public abstract Builder serial(String serial);

        public abstract Builder subject(String subject);

        public abstract Builder issuer(String issuer);

        public abstract Builder commonName(String commonName);

        public abstract Builder subjectAlternativeNames(List<String> subjectAlternativeNames);

        public abstract Builder dnsNames(List<String> dnsNames);

        public abstract Builder notBefore(DateTime notBefore);

        public abstract Builder notAfter(DateTime notAfter);

        public abstract Builder isCa(Boolean isCa);

        public abstract Builder selfSigned(Boolean selfSigned);

        public abstract Builder publicKeyAlgorithm(String publicKeyAlgorithm);

        public abstract Builder publicKeyBits(Integer publicKeyBits);

        public abstract Builder signatureAlgorithm(String signatureAlgorithm);

        public abstract Builder extendedKeyUsage(List<String> extendedKeyUsage);

        public abstract Builder subjectKeyIdentifier(byte[] subjectKeyIdentifier);

        public abstract Builder authorityKeyIdentifier(byte[] authorityKeyIdentifier);

        public abstract CertificateDetails build();
    }
}
