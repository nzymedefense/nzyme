package app.nzyme.core.crypto.tls;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class TLSKeyAndCertificate {

    public abstract UUID nodeId();
    public abstract List<X509Certificate> certificates();
    public abstract PrivateKey key();
    public abstract String signature();
    public abstract DateTime validFrom();
    public abstract DateTime expiresAt();

    public static TLSKeyAndCertificate create(UUID nodeId, List<X509Certificate> certificates, PrivateKey key, String signature, DateTime validFrom, DateTime expiresAt) {
        return builder()
                .nodeId(nodeId)
                .certificates(certificates)
                .key(key)
                .signature(signature)
                .validFrom(validFrom)
                .expiresAt(expiresAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TLSKeyAndCertificate.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder nodeId(UUID nodeId);

        public abstract Builder certificates(List<X509Certificate> certificates);

        public abstract Builder key(PrivateKey key);

        public abstract Builder signature(String signature);

        public abstract Builder validFrom(DateTime validFrom);

        public abstract Builder expiresAt(DateTime expiresAt);

        public abstract TLSKeyAndCertificate build();
    }

}
