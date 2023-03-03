package app.nzyme.core.crypto.database;

import app.nzyme.core.crypto.tls.TLSSourceType;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class TLSWildcardKeyAndCertificateEntry {

    public abstract long id();
    public abstract String nodeMatcher();
    public abstract String certificate();
    public abstract String key();
    public abstract TLSSourceType sourceType();
    public abstract DateTime validFrom();
    public abstract DateTime expiresAt();

    public static TLSWildcardKeyAndCertificateEntry create(long id, String nodeMatcher, String certificate, String key, TLSSourceType sourceType, DateTime validFrom, DateTime expiresAt) {
        return builder()
                .id(id)
                .nodeMatcher(nodeMatcher)
                .certificate(certificate)
                .key(key)
                .sourceType(sourceType)
                .validFrom(validFrom)
                .expiresAt(expiresAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TLSWildcardKeyAndCertificateEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder nodeMatcher(String nodeMatcher);

        public abstract Builder certificate(String certificate);

        public abstract Builder key(String key);

        public abstract Builder sourceType(TLSSourceType sourceType);

        public abstract Builder validFrom(DateTime validFrom);

        public abstract Builder expiresAt(DateTime expiresAt);

        public abstract TLSWildcardKeyAndCertificateEntry build();
    }

}
