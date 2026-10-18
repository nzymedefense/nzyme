package app.nzyme.core.ethernet.tls.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class TLSCertificateDetailsEntry {

    public abstract String sha256();
    @Nullable
    public abstract String sha1();
    public abstract boolean parsed();
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
    public abstract String subjectKeyIdentifier();
    @Nullable
    public abstract String authorityKeyIdentifier();
    public abstract byte[] der();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();
    public abstract long sessionCount();
    public abstract List<String> sources();

    public static Builder builder() {
        return new AutoValue_TLSCertificateDetailsEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sha256(String sha256);
        public abstract Builder sha1(String sha1);
        public abstract Builder parsed(boolean parsed);
        public abstract Builder parseError(String parseError);
        public abstract Builder version(Integer version);
        public abstract Builder serial(String serial);
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
        public abstract Builder subjectKeyIdentifier(String subjectKeyIdentifier);
        public abstract Builder authorityKeyIdentifier(String authorityKeyIdentifier);
        public abstract Builder der(byte[] der);
        public abstract Builder firstSeen(DateTime firstSeen);
        public abstract Builder lastSeen(DateTime lastSeen);
        public abstract Builder sessionCount(long sessionCount);
        public abstract Builder sources(List<String> sources);
        public abstract TLSCertificateDetailsEntry build();
    }
}
