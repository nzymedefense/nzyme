package app.nzyme.core.crypto.tls;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

@AutoValue
public abstract class TLSWildcardKeyAndCertificate {

    @Nullable
    public abstract Long id();
    public abstract String nodeMatcher();
    public abstract TLSSourceType sourceType();
    public abstract List<X509Certificate> certificates();
    public abstract PrivateKey key();
    public abstract String signature();
    public abstract DateTime validFrom();
    public abstract DateTime expiresAt();

    public static TLSWildcardKeyAndCertificate create(Long id, String nodeMatcher, TLSSourceType sourceType, List<X509Certificate> certificates, PrivateKey key, String signature, DateTime validFrom, DateTime expiresAt) {
        return builder()
                .id(id)
                .nodeMatcher(nodeMatcher)
                .sourceType(sourceType)
                .certificates(certificates)
                .key(key)
                .signature(signature)
                .validFrom(validFrom)
                .expiresAt(expiresAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TLSWildcardKeyAndCertificate.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder nodeMatcher(String nodeMatcher);

        public abstract Builder sourceType(TLSSourceType sourceType);

        public abstract Builder certificates(List<X509Certificate> certificates);

        public abstract Builder key(PrivateKey key);

        public abstract Builder signature(String signature);

        public abstract Builder validFrom(DateTime validFrom);

        public abstract Builder expiresAt(DateTime expiresAt);

        public abstract TLSWildcardKeyAndCertificate build();
    }

}
