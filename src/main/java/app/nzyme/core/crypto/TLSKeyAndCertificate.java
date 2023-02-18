package app.nzyme.core.crypto;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@AutoValue
public abstract class TLSKeyAndCertificate {

    public abstract X509Certificate certificate();
    public abstract PrivateKey key();
    public abstract DateTime validFrom();
    public abstract DateTime expiresAt();

    public static TLSKeyAndCertificate create(X509Certificate certificate, PrivateKey key, DateTime validFrom, DateTime expiresAt) {
        return builder()
                .certificate(certificate)
                .key(key)
                .validFrom(validFrom)
                .expiresAt(expiresAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TLSKeyAndCertificate.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder certificate(X509Certificate certificate);

        public abstract Builder key(PrivateKey key);

        public abstract Builder validFrom(DateTime validFrom);

        public abstract Builder expiresAt(DateTime expiresAt);

        public abstract TLSKeyAndCertificate build();
    }

}
