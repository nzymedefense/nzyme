package app.nzyme.core.ethernet.tls.db;

import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class TLSChainCertificateEntry {

    public abstract String sha256();
    public abstract boolean parsed();
    @Nullable
    public abstract String subject();
    @Nullable
    public abstract String issuer();
    @Nullable
    public abstract String commonName();
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

    public static Builder builder() {
        return new AutoValue_TLSChainCertificateEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder sha256(String sha256);
        public abstract Builder parsed(boolean parsed);
        public abstract Builder subject(String subject);
        public abstract Builder issuer(String issuer);
        public abstract Builder commonName(String commonName);
        public abstract Builder notBefore(DateTime notBefore);
        public abstract Builder notAfter(DateTime notAfter);
        public abstract Builder isCa(Boolean isCa);
        public abstract Builder selfSigned(Boolean selfSigned);
        public abstract Builder publicKeyAlgorithm(String publicKeyAlgorithm);
        public abstract Builder publicKeyBits(Integer publicKeyBits);
        public abstract Builder signatureAlgorithm(String signatureAlgorithm);
        public abstract TLSChainCertificateEntry build();
    }
}
